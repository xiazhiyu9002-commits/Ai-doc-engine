package com.aidoc.engine.service.impl;

import java.io.InputStream;

public class ResourceCheck {
    public static void main(String[] args) {
        String[] paths = {
            "org/docx4j/math/MML2OMML.XSL",
            "MML2OMML.XSL",
            "org/docx4j/math/mml2omml.xsl"
        };
        
        for (String path : paths) {
            InputStream is = ResourceCheck.class.getClassLoader().getResourceAsStream(path);
            System.out.println(path + " -> " + (is != null ? "FOUND" : "NOT FOUND"));
            if (is != null) {
                try { is.close(); } catch (Exception e) {}
            }
        }
    }
}
