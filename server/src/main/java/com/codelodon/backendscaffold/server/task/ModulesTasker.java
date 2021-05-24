package com.codelodon.backendscaffold.server.task;

import com.codelodon.backendscaffold.common.entity.Blocked;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import com.codelodon.backendscaffold.common.entity.SubSystemAnno;
import com.codelodon.backendscaffold.dto.main.entity.HttpMethod;
import com.codelodon.backendscaffold.dto.main.model.Module;
import com.codelodon.backendscaffold.dto.main.model.Operation;
import com.codelodon.backendscaffold.dto.main.model.SubSystem;
import com.codelodon.backendscaffold.server.module.main.dao.ModuleRepo;
import com.codelodon.backendscaffold.server.module.main.dao.OperationRepo;
import com.codelodon.backendscaffold.server.module.main.dao.SubSystemRepo;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class ModulesTasker implements ApplicationListener<ContextRefreshedEvent> {
    final private Logger logger = LoggerFactory.getLogger(ModulesTasker.class);
    final private Environment env;
    final private SubSystemRepo subSystemRepo;
    final private ModuleRepo moduleRepo;
    final private OperationRepo operationRepo;

    @Autowired
    public ModulesTasker(Environment env, SubSystemRepo subSystemRepo, ModuleRepo moduleRepo, OperationRepo operationRepo) {
        this.env = env;
        this.subSystemRepo = subSystemRepo;
        this.moduleRepo = moduleRepo;
        this.operationRepo = operationRepo;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        List<String> envList = Arrays.asList(env.getActiveProfiles());
        if (envList.contains("test") || envList.contains("prod")) {
            return;
        }

        ApplicationContext context = contextRefreshedEvent.getApplicationContext();

        // 获取子系统 Bean
        Map<String, Object> subSystemBeans = context.getBeansWithAnnotation(SubSystemAnno.class);
        for (Map.Entry<String, Object> subSystemEntry : subSystemBeans.entrySet()) {

            BeanDefinition subSystemDefinition = ((AnnotationConfigServletWebServerApplicationContext) context).getBeanDefinition(subSystemEntry.getKey());
            AnnotatedTypeMetadata subSystemAnnotation = (AnnotatedTypeMetadata) subSystemDefinition.getSource();

            if (null != subSystemAnnotation) {
                Map<String, Object> subSystemAttributes = subSystemAnnotation.getAnnotationAttributes(SubSystemAnno.class.getName());
                String systemName = subSystemAttributes.get("systemName").toString();
                String packageName = subSystemAttributes.get("packageName").toString();

                logger.info("子系统名称 {}, 子系统包名 {}", systemName, packageName);
                // 子系统入库
                SubSystem subSystemRecord = this.saveSubSystemIfNotExist(systemName, packageName);
                logger.info("子系统记录 {}", subSystemRecord);

                // 获取模块 Bean (controller)
                Reflections reflections = new Reflections(packageName);
                Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(ModuleAnno.class);
                for (Class<?> controllerClass : controllerClasses) {
                    ModuleAnno m = controllerClass.getAnnotation(ModuleAnno.class);
                    String moduleName = m.name();
                    String fullClassname = controllerClass.getCanonicalName();

                    logger.info("模块类名 {}, 模块完整类名 {}", moduleName, fullClassname);
                    // 模块入库
                    Module moduleRecord = this.saveModuleIfNotExist(moduleName, fullClassname, subSystemRecord);
                    logger.info("模块记录 {}", moduleRecord);

                    // 获取每个模块可用的接口
                    Method[] methods = controllerClass.getMethods();
                    for (Method method : methods) {
                        OperationAnno operation = method.getAnnotation(OperationAnno.class);
                        String operationName = null, httpMethod = null;
                        boolean needAuth = false;

                        if (null != operation) { // 如果直接能拿到, 要么是自定义的接口, 要么是重新注解的复写的父级接口
                            operationName = operation.name();
                            httpMethod = operation.httpMethod();
                            needAuth = operation.needAuth();
                        } else { // 如果本类没有被注解的接口, 那么就认为父级为BaseController

                            // 有的接口复写了是为了返回 404 信息, 这种接口需要屏蔽掉
                            Blocked blocked = method.getAnnotation(Blocked.class);
                            if (null != blocked) {
                                continue;
                            }

                            try {
                                Method superMethod = controllerClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
                                operation = superMethod.getAnnotation(OperationAnno.class);
                                if (null == operation) { // 父级也没有的话就不管了
                                    continue;
                                }

                                operationName = operation.name();
                                httpMethod = operation.httpMethod();
                                needAuth = operation.needAuth();
                            } catch (NoSuchMethodException e) {
                                logger.error("jesus christ, why the fuck needs to try catch this shit, you just need return null value, fucking trash.", e);
                            }
                        }

                        if (StringUtils.isNotEmpty(operationName) && StringUtils.isNotEmpty(httpMethod)) {
                            String methodName = method.getName();
                            logger.info("接口所属类名{}, 接口名称 {}, 接口方法名 {}, 接口请求方式 {}, 接口是否需要鉴权 {}", fullClassname, operationName, methodName, httpMethod, needAuth);

                            // 接口入库
                            this.saveOperationIfNotExist(fullClassname, operationName, methodName, httpMethod, needAuth, moduleRecord);

                        }
                    }
                }
            }
        }
    }

    private SubSystem saveSubSystemIfNotExist(final String name, final String packageName) {
        Optional<SubSystem> optionalSubSystem = subSystemRepo.findByNameAndPackageName(name, packageName);
        if (optionalSubSystem.isPresent()) {
            return optionalSubSystem.get();
        }

        SubSystem subSystem = new SubSystem();
        subSystem.setName(name);
        subSystem.setPackageName(packageName);

        return subSystemRepo.save(subSystem);
    }

    private Module saveModuleIfNotExist(
            final String name,
            final String fullClassname,
            final SubSystem subSystem
    ) {
        Optional<Module> optionalModule = moduleRepo.findByNameAndFullClassname(name, fullClassname);
        if (optionalModule.isPresent()) {
            return optionalModule.get();
        }

        Module module = new Module();
        module.setName(name);
        module.setFullClassname(fullClassname);
        module.setSubSystem(subSystem);

        return moduleRepo.save(module);
    }

    private void saveOperationIfNotExist(
            final String fullClassname,
            final String name,
            final String methodName,
            final String httpMethod,
            final Boolean needAuth,
            final Module module
    ) {
        HttpMethod requestMethod = HttpMethod.valueOf(httpMethod);
        Optional<Operation> optionalOperation = operationRepo.findHuh(fullClassname, name, methodName, requestMethod);
        if (optionalOperation.isPresent()) {
            return;
        }

        Operation operation = new Operation();
        operation.setFullClassName(fullClassname);
        operation.setName(name);
        operation.setMethodName(methodName);
        operation.setType(requestMethod);
        operation.setNeedAuth(needAuth);
        operation.setModule(module);

        operationRepo.save(operation);
    }
}
