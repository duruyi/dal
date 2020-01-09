package com.ctrip.platform.dal.dao.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ctrip.platform.dal.exceptions.DalRuntimeException;
import net.sf.cglib.core.ReflectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.ctrip.platform.dal.dao.annotation.DalTransactional;
import com.ctrip.platform.dal.dao.annotation.Transactional;

public class DalTransactionalEnabler implements ImportBeanDefinitionRegistrar, BeanFactoryPostProcessor {
    private static final String BEAN_VALIDATOR_NAME = DalAnnotationValidator.VALIDATOR_NAME;
    private static final String BEAN_FACTORY_NAME = DalTransactionManager.class.getName();
    private static final String FACTORY_METHOD_NAME = "create";

    private BeanDefinitionRegistry registry;
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.registry = registry;
        register();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        register();
    }

    private void register() {
        replaceBeanDefinition();
        registerValidator();
    }

    private String[] getBeanDefinitionNames() {
        return registry == null ? beanFactory.getBeanDefinitionNames() : registry.getBeanDefinitionNames();
    }

    private BeanDefinition getBeanDefinition(String beanName) {
        return registry == null ? beanFactory.getBeanDefinition(beanName) : registry.getBeanDefinition(beanName);
    }

    private void replaceBeanDefinition() {
        for (String beanName : getBeanDefinitionNames()) {
            BeanDefinition beanDef = getBeanDefinition(beanName);
            String beanClassName = beanDef.getBeanClassName();

            if (beanClassName == null || beanClassName.equals(BEAN_FACTORY_NAME))
                continue;

            Class beanClass;
            try {
                beanClass = Class.forName(beanDef.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new BeanDefinitionValidationException("Cannot validate bean: " + beanName, e);
            }

            boolean annotated = false;

            List<Method> methods = new ArrayList<>();
            ReflectUtils.addAllMethods(beanClass, methods);
            List<Method> unsupportedMethods = new ArrayList<>();

            for (int i = 0; i < methods.size(); i++) {
                Method currentMethod = methods.get(i);

                if (isTransactionAnnotated(currentMethod)) {
                    if (Modifier.isFinal(currentMethod.getModifiers()) || Modifier.isStatic(currentMethod.getModifiers()) || Modifier.isPrivate(currentMethod.getModifiers()))
                        unsupportedMethods.add(currentMethod);
                    annotated = true;
                }
            }

            if (!unsupportedMethods.isEmpty()){
                StringBuilder errMsg=new StringBuilder();
                errMsg.append(String.format("The Methods below are not supported in dal transaction due to private, final or static modifier, please use public，protected or default modifier instead："));
                errMsg.append("\n");
                int index=1;
                for (Method method : unsupportedMethods) {
                    errMsg.append(String.format("%d. %s", index, method.toString()));
                    errMsg.append("\n");
                    index++;
                }
                throw new DalRuntimeException(errMsg.toString());
            }

            if (!annotated)
                continue;

            beanDef.setBeanClassName(BEAN_FACTORY_NAME);
            beanDef.setFactoryMethodName(FACTORY_METHOD_NAME);

            ConstructorArgumentValues cav = beanDef.getConstructorArgumentValues();

            if (cav.getArgumentCount() != 0)
                throw new BeanDefinitionValidationException("The transactional bean can only be instantiated with default constructor.");

            cav.addGenericArgumentValue(beanClass.getName());
        }
    }


    private boolean isTransactionAnnotated(Method method) {
        return method.isAnnotationPresent(Transactional.class) || method.isAnnotationPresent(DalTransactional.class);
    }

    private void registerValidator() {
        if (registry != null) {
            // Need to check here because bean name canbe low case
            if (registry.containsBeanDefinition(BEAN_VALIDATOR_NAME))
                return;

            for (String beanName : registry.getBeanDefinitionNames()) {
                BeanDefinition beanDef = registry.getBeanDefinition(beanName);
                if (Objects.equals(beanDef.getBeanClassName(), BEAN_VALIDATOR_NAME))
                    return;
            }

            registry.registerBeanDefinition(BEAN_VALIDATOR_NAME, BeanDefinitionBuilder.genericBeanDefinition(DalAnnotationValidator.class).getBeanDefinition());
        } else {
            // Need to check here because bean name canbe low case
            if (beanFactory.containsBeanDefinition(BEAN_VALIDATOR_NAME))
                return;

            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
                if (Objects.equals(beanDef.getBeanClassName(), BEAN_VALIDATOR_NAME))
                    return;
            }

            beanFactory.addBeanPostProcessor(new DalAnnotationValidator());
        }
    }
}