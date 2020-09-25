package org.dizitart.no2.rx;

import io.reactivex.internal.operators.maybe.MaybeFromCallable;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FlowableWriteResultTest {
    @Test
    public void testGetAffectedCount() {
        assertTrue((new FlowableWriteResult(new MaybeFromCallable<>(
            new MaybeFromCallable<>(new MaybeFromCallable<>(null)))))
                .getAffectedCount() instanceof io.reactivex.internal.operators.single.SingleFromCallable);
    }
}

