package com.ctrip.framework.dal.dbconfig.plugin.util;

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by shenjie on 2019/4/30.
 */
public class XmlUtils {

    // ignore character escape
    private static final CharacterEscapeHandler characterNoEscapeHandler = new CharacterEscapeHandler() {
        @Override
        public void escape(char[] ch, int start,int length, boolean isAttVal, Writer writer) throws IOException {
            writer.write(ch, start, length);
        }
    };

    /**
     * 将对象直接转换成String类型的 XML输出
     *
     * @param obj
     * @return
     */
    public static String toXml(Object obj) throws Exception {
        // 创建输出流
        StringWriter sw = new StringWriter();
        // 利用jdk中自带的转换类实现
        JAXBContext context = JAXBContext.newInstance(obj.getClass());

        Marshaller marshaller = context.createMarshaller();
        // 格式化xml输出的格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        marshaller.setProperty(CharacterEscapeHandler.class.getName(), characterNoEscapeHandler);
        // 将对象转换成输出流形式的xml
        marshaller.marshal(obj, sw);

        return sw.toString();
    }

    /**
     * 将String类型的xml转换成对象
     */
    public static Object fromXml(String xmlContent, Class clazz) throws Exception {
        Object xmlObject = null;
        JAXBContext context = JAXBContext.newInstance(clazz);
        // 进行将Xml转成对象的核心接口
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader sr = new StringReader(xmlContent);
        xmlObject = unmarshaller.unmarshal(sr);
        return xmlObject;
    }

}
