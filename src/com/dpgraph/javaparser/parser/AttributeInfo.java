package com.dpgraph.javaparser.parser;

public class AttributeInfo {
        private String name;
        private byte[] value;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }

        public byte[] getValue() {
            return value;
        }
    }
