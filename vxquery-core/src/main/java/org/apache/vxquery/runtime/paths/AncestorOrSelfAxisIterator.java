/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.vxquery.runtime.paths;

import org.apache.vxquery.exceptions.SystemException;
import org.apache.vxquery.runtime.CallStackFrame;
import org.apache.vxquery.runtime.LocalRegisterAccessor;
import org.apache.vxquery.runtime.RegisterAllocator;
import org.apache.vxquery.runtime.RegisterSet;
import org.apache.vxquery.runtime.base.AbstractLazilyEvaluatedIterator;
import org.apache.vxquery.runtime.base.RuntimeIterator;
import org.apache.vxquery.util.Filter;
import org.apache.vxquery.v0datamodel.XDMNode;
import org.apache.vxquery.v0datamodel.XDMValue;

public class AncestorOrSelfAxisIterator extends AbstractLazilyEvaluatedIterator {
    private final RuntimeIterator input;
    private final Filter<XDMValue> filter;
    private final LocalRegisterAccessor<XDMNode> currentNode;

    public AncestorOrSelfAxisIterator(RegisterAllocator rAllocator, RuntimeIterator input, Filter<XDMValue> filter) {
        super(rAllocator);
        this.input = input;
        this.filter = filter;
        currentNode = new LocalRegisterAccessor<XDMNode>(rAllocator.allocate(1));
    }

    @Override
    public void open(CallStackFrame frame) {
        input.open(frame);
        currentNode.set(frame, null);
    }

    @Override
    public void close(CallStackFrame frame) {
        input.close(frame);
    }

    @Override
    public Object next(CallStackFrame frame) throws SystemException {
        final RegisterSet regs = frame.getLocalRegisters();
        while (true) {
            XDMNode cNode = currentNode.get(regs);
            if (cNode == null) {
                cNode = (XDMNode) input.next(frame);
                if (cNode == null) {
                    return null;
                }
            }
            currentNode.set(regs, cNode.getParent());
            if (filter.accept(cNode)) {
                return cNode;
            }
        }
    }
}