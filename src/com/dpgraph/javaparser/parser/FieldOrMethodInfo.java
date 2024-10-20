package com.dpgraph.javaparser.parser;

public class FieldOrMethodInfo {
    private final int _accessFlags;
    private final int _nameIndex;
    private final int _descriptorIndex;
    private AttributeInfo _runtimeVisibleAnnotations;
    private AttributeInfo _signature;

    public FieldOrMethodInfo(int accessFlags, int nameIndex, int descriptorIndex) {
        _accessFlags = accessFlags;
        _nameIndex = nameIndex;
        _descriptorIndex = descriptorIndex;
    }

    public int getAccessFlags() {
        return _accessFlags;
    }

    public int getNameIndex() {
        return _nameIndex;
    }

    public int getDescriptorIndex() {
        return _descriptorIndex;
    }

    public AttributeInfo getSignature() {
            return _signature;
        }

    public AttributeInfo getRuntimeVisibleAnnotations() {
        return _runtimeVisibleAnnotations;
    }

    public void setRuntimeVisibleAnnotations(AttributeInfo _runtimeVisibleAnnotations) {
        this._runtimeVisibleAnnotations = _runtimeVisibleAnnotations;
    }

    public void setSignature(AttributeInfo _signature) {
        this._signature = _signature;
    }
}
