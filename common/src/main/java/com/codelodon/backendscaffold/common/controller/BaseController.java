package com.codelodon.backendscaffold.common.controller;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.CreateGroup;
import com.codelodon.backendscaffold.common.entity.ModifyGroup;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.*;

/**
 * 基础查询控制器类
 * 包含查询一条数据，条件查询分页列表，新增，修改，删除接口
 *
 * @param <T>  表实体类型
 * @param <ID> 表实体主键类型
 */
public class BaseController<T, ID> {
    final private Logger logger = LoggerFactory.getLogger(BaseController.class);
    final private BaseRepo<T, ID> repo;

    /**
     * 查询条件对应 JPA CriteriaBuilder 的方法
     * 这里抄袭一下 strapi.js 的格式
     * 没有后缀或者 eq: equal
     * ne: notEqual
     * lt: lessThan
     * gt: greaterThan
     * lte: lessThanOrEqualTo
     * gte: greaterThanOrEqualTo
     * in: in，用半角逗号分隔
     * null: isNull
     * notnull: isNotNull
     * like: like，用于模糊查询的 % 要自己拼接，这里不会自动拼接
     */
    final private Map<String, Map<String, Class[]>> filterCallMap = new HashMap<>() {{
        put("eq", Map.of("equal", new Class[]{Expression.class, Object.class}));
        put("ne", Map.of("notEqual", new Class[]{Expression.class, Object.class}));
        put("lt", Map.of("lessThan", new Class[]{Expression.class, Comparable.class}));
        put("gt", Map.of("greaterThan", new Class[]{Expression.class, Comparable.class}));
        put("lte", Map.of("lessThanOrEqualTo", new Class[]{Expression.class, Comparable.class}));
        put("gte", Map.of("greaterThanOrEqualTo", new Class[]{Expression.class, Comparable.class}));
        put("in", Map.of("in", new Class[]{Expression.class}));
        put("null", Map.of("isNull", new Class[]{Expression.class}));
        put("notnull", Map.of("isNotNull", new Class[]{Expression.class}));
        put("like", Map.of("like", new Class[]{Expression.class, String.class}));
    }};

    public BaseController(BaseRepo<T, ID> repo) {
        logger.debug("父级构造被调用");
        this.repo = repo;
    }

    /**
     * 按照ID查询单条记录
     *
     * @param id 记录ID
     * @return 对应记录
     */
    @Path("/{id}")
    @GET
    @OperationAnno(name = "按ID查询", httpMethod = "GET")
    public Response getOne(@PathParam("id") final ID id) {
        Optional<T> t = repo.findById(id);
        return Response.ok().entity(new BaseResponse(true, "查询成功", t.orElse(null))).build();
    }

    /**
     * 查询列表
     * 支持条件查询，支持 =，!=，>，>=，<，<=，in，like，is null，is not null
     * 查询条件格式见 filterCallMap
     *
     * @param pageSize 每页记录条数，最小为1
     * @param pageNum  当前页码，从1开始
     * @param orderBy  排序字段，实体字段名，非表字段名
     * @param seq      排序方式，DESC 或 ASC
     * @param ps       查询条件，请求 URL 比如： /list;id_eq=1;username_like=%admin%;password_null;
     * @return 分页记录列表，count 总记录条数，list 记录列表
     */
    @Path("/{var:list}")
    @GET
    @OperationAnno(name = "查询列表", httpMethod = "GET")
    public Response getList(@QueryParam("pageSize")
                            @NotNull(message = "每页条数不能为空")
                            @Min(value = 1, message = "每页条数至少为1") final Integer pageSize,
                            @QueryParam("pageNum")
                            @NotNull(message = "当前页码不能为空")
                            @Min(value = 1, message = "当前页码至少为1") final Integer pageNum,
                            @QueryParam("orderBy")
                            @NotBlank(message = "排序字段不能为空") final String orderBy,
                            @QueryParam("seq")
                            @DefaultValue("DESC") final String seq,
                            @PathParam("var") PathSegment ps) {
        Specification<T> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            MultivaluedMap<String, String> mm = ps.getMatrixParameters();
            // 因为有可能一个字段多个条件，所以值是多个
            for (Map.Entry<String, List<String>> entry : mm.entrySet()) {
                String key = entry.getKey();
                int splitIndex = key.lastIndexOf("_");
                String filter = "eq";

                if (splitIndex < 0) {
                    splitIndex = key.length();
                } else {
                    filter = key.substring(key.lastIndexOf("_") + 1);
                }

                Map<String, Class[]> call = filterCallMap.get(filter);
                String methodName = call.keySet().iterator().next();
                Class[] paramTypes = call.get(methodName);

                List<String> values = entry.getValue();
                for (String value : values) {
                    String columnName = key.substring(0, splitIndex);
                    boolean isColumnEnum = false;
                    boolean isDate = false;
                    Class<?> paramClazz;

                    Expression<T> expression = null;
                    try {
                        if (columnName.contains(".")) {

                            String[] splits = columnName.split("\\.");
                            String joinColumnName = splits[splits.length - 1];

                            Class<?> fieldClazz = root.getModel().getJavaType();

                            for (int i = 0; i < splits.length - 1; i++) {
                                expression = null == expression ? root.join(splits[i]) : ((Join) expression).join(splits[i]);

                                Class<?> type = fieldClazz.getDeclaredField(splits[i]).getType();
                                if (type == Set.class) {
                                    fieldClazz = (Class<T>) ((ParameterizedType) fieldClazz.getDeclaredField(splits[i]).getGenericType()).getActualTypeArguments()[0];
                                } else {
                                    fieldClazz = type;
                                }
                            }

                            paramClazz = fieldClazz.getDeclaredField(joinColumnName).getType();

                            if (paramClazz.isEnum()) {
                                isColumnEnum = true;
                            } else if (paramClazz.equals(Timestamp.class) || paramClazz.equals(Date.class)) {
                                isDate = true;
                            }

                            expression = null == expression ? root.get(joinColumnName) : ((Join) expression).get(joinColumnName);
                        } else {

                            paramClazz = root.getModel().getAttribute(columnName).getJavaType();
                            if (paramClazz.isEnum()) {
                                isColumnEnum = true;
                            } else if (paramClazz.equals(Timestamp.class) || paramClazz.equals(Date.class)) {
                                isDate = true;
                            }

                            expression = root.get(columnName);
                        }
                    } catch (IllegalArgumentException | NoSuchFieldException e) {
                        logger.error("查询字段在实体中不存在，跳过此条件", e);
                        break;
                    }

                    Object predicate;

                    if ("in".equals(filter)) {
                        if (isColumnEnum) {
                            String[] vArr = value.split(",");
                            List<Object> pList = new ArrayList<>(vArr.length);
                            Enum[] enums = (Enum[]) paramClazz.getEnumConstants();
                            for (String vItem : vArr) {
                                if (null != vItem && !"".equals(vItem)) {
                                    Enum p = Arrays.stream(enums).filter(t -> t.toString().equals(vItem)).findFirst().orElse(null);
                                    pList.add(p);
                                }
                            }
                            predicate = expression.in(pList.toArray());

                        } else if (isDate) {
                            String[] strings = value.split(",");
                            Date[] params = new Date[strings.length];
                            for (int i = 0; i < strings.length; i++) {
                                params[i] = new Date(Long.parseLong(strings[i]));
                            }
                            predicate = expression.in((Object[]) params);
                        } else {
                            predicate = expression.in((Object[]) value.split(","));
                        }
                    } else {
                        Object[] params;

                        // 要调用的方法形参有多少，这里就传多少
                        if (1 == paramTypes.length) {
                            params = new Object[]{expression};
                        } else {
                            if (isColumnEnum) {
                                Enum[] enums = (Enum[]) paramClazz.getEnumConstants();
                                Enum p = Arrays.stream(enums).filter(t -> t.toString().equals(value)).findFirst().orElse(null);
                                params = new Object[]{expression, p};

                            } else if (isDate) {
                                params = new Object[]{expression, new Date(Long.parseLong(value))};
                            } else {
                                params = new Object[]{expression, value};
                            }
                        }

                        predicate = buildCriteria(root, criteriaBuilder, methodName, paramTypes, params);
                    }

                    if (null != predicate) {
                        predicates.add((Predicate) predicate);
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(orderBy);
        sort = "DESC".equals(seq) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        Page<T> page = repo.findAll(specification, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("count", page.getTotalElements());
        result.put("list", page.getContent());
        return Response.ok().entity(new BaseResponse(true, "查询成功", result)).build();
    }

    /**
     * 构建查询条件
     *
     * @param root            Huh, root?
     * @param criteriaBuilder CriteriaBuilder 对象，hmm
     * @param methodName      调用方法名
     * @param paramTypes      参数类型数组
     * @param params          参数数组
     */
    private Object buildCriteria(Root<T> root, CriteriaBuilder criteriaBuilder, String methodName, Class[] paramTypes, Object[] params) {
        Object predicate = null;
        try {
            predicate = criteriaBuilder.getClass().getDeclaredMethod(methodName, paramTypes).invoke(criteriaBuilder, params);
        } catch (IllegalAccessException e) {
            logger.error("CriteriaBuilder 的目标方法无法访问，跳过此查询条件", e);
        } catch (InvocationTargetException e) {
            logger.error("CriteriaBuilder 的目标方法调用失败，跳过此查询条件", e);
        } catch (NoSuchMethodException e) {
            logger.error("CriteriaBuilder 的目标方法不存在，跳过此查询条件", e);
        }
        return predicate;
    }

    /**
     * 新增记录
     *
     * @param t 记录实体
     * @return 保存后的记录
     */
    @POST
    @OperationAnno(name = "新增", httpMethod = "POST")
    public Response create(@Valid @ConvertGroup(from = Default.class, to = CreateGroup.class) final T t) {
        T nt = repo.save(t);
        return Response.ok().entity(new BaseResponse(true, "创建成功", nt)).build();
    }

    /**
     * 修改记录
     *
     * @param id 原记录ID
     * @param t  需要更新的记录
     * @return 更新后的记录
     */
    @Path("/{id}")
    @PUT
    @OperationAnno(name = "根据ID修改", httpMethod = "PUT")
    public Response modify(@PathParam("id") final ID id, @Valid @ConvertGroup(from = Default.class, to = ModifyGroup.class) final T t) {
        // 假删除用更新，毕竟假删除不是硬性标准，谁实现谁维护
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        T nt = repo.save(t);
        return Response.ok().entity(new BaseResponse(true, "修改成功", nt)).build();
    }

    /**
     * 删除记录
     *
     * @param id 原记录ID
     * @return 无
     */
    @Path("/{id}")
    @DELETE
    @OperationAnno(name = "根据ID删除", httpMethod = "DELETE")
    public Response delete(@PathParam("id") final ID id) {
        // 这就是真删除，不要把接口做的那么迷惑人，假删除用更新接口就行，不爽就自己写
        if (!repo.existsById(id)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }
        repo.deleteById(id);
        return Response.ok().entity(new BaseResponse(true, "删除成功", null)).build();
    }
}
