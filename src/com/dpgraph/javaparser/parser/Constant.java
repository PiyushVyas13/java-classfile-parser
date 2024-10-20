package com.dpgraph.javaparser.parser;

public class Constant {
    private final byte _tag;
    private final int _nameIndex;
    private final int _typeIndex;
    private Object _value;

    public Constant(byte tag, int nameIndex) {
        this._tag = tag;
        this._nameIndex = nameIndex;
        this._typeIndex = -1;
    }

    public Constant(byte tag, Object value) {
        this(tag, -1);
        _value = value;
    }

    Constant(byte tag, int nameIndex, int typeIndex) {
        _tag = tag;
        _nameIndex = nameIndex;
        _typeIndex = typeIndex;
        _value = null;
    }

    public byte getTag() {
        return _tag;
    }

    public int getNameIndex() {
        return _nameIndex;
    }

    public int getTypeIndex() {
        return _typeIndex;
    }

    public Object getValue() {
        return _value;
    }

    @Override
    public String toString() {
        return "com.dpgraph.parser.Constant{" +
                "_tag=" + _tag +
                ", _nameIndex=" + _nameIndex +
                ", _typeIndex=" + _typeIndex +
                ", _value=" + _value +
                '}';
        }
    }
