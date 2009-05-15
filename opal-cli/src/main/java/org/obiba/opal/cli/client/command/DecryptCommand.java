/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import org.obiba.opal.cli.client.command.options.DecryptCommandOptions;
import org.obiba.opal.core.datasource.onyx.IOnyxDataInputStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Command to decrypt an Onyx data file.
 */
public class DecryptCommand extends AbstractCommand<DecryptCommandOptions> {
  //
  // Instance Variables
  //

  private IOnyxDataInputStrategy dataInputStrategy;

  //
  // AbstractCommand Methods
  //

  public void execute() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    // If user name and/or password have not been provided, prompt for them now.
    if(!options.isKeyStorePassword()) {
      // prompt for keyStorePassword
    }

    // First, lazily initialize the dataInputStrategy variable (fetch it from the Spring ApplicationContext).
    ApplicationContext context = loadContext();
    setDataInputStrategy((IOnyxDataInputStrategy) context.getBean("onyxDataInputStrategy"));

    System.out.println("<decrypt: " + dataInputStrategy.getClass().getSimpleName() + ">");
  }

  //
  // Methods
  //

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  private ApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext("spring/opal-cli/context.xml");
  }
}
