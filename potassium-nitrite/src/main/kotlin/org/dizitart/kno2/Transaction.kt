/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.kno2

import org.dizitart.no2.Nitrite
import org.dizitart.no2.transaction.Session
import org.dizitart.no2.transaction.Transaction

/**
 *
 * @author Anindya Chatterjee
 */
fun Nitrite.session(op: (Session.() -> Unit)? = null): Session {
    val session = this.createSession()
    op?.invoke(session)
    session.close()
    return session
}

fun Session.tx(op: (Transaction.() -> Unit)? = null): Transaction {
    val tx = this.beginTransaction()
    op?.invoke(tx)
    tx.close()
    return tx
}