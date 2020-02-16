/*
 * MshFieldOne.java
 * 
 * Copyright (C) 2007-2012 M Litherland
 */

package org.nule.lighthl7lib.hl7;

/**
 *
 * @author mike
 */
public class MshFieldOne extends Hl7Field {
    
    /** Creates a new instance of MshFieldOne */
    public MshFieldOne(String[] separators) {
        super(separators[4], separators);
    }

    @Override
    protected Hl7Field getReal(int id, int sep) {
        return this;
    }

    @Override
    protected int getRealCount(int sep) {
        return 1;
    }

    @Override
    public String toString() {
        return seps[4];
    }
    
}
