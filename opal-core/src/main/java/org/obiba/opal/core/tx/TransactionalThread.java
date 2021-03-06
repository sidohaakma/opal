/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tx;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalThread extends Thread {

  private final TransactionTemplate transactionTemplate;

  private final Runnable runnable;

  public TransactionalThread(TransactionTemplate transactionTemplate, Runnable runnable) {
    super("transactional-thread-" + runnable.getClass().getName());
    this.transactionTemplate = transactionTemplate;
    this.runnable = runnable;
    setPriority(MIN_PRIORITY);
  }

  @Override
  public void run() {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        runnable.run();
      }
    });
  }
}