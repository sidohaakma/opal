/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the import VCF command.
 */
@CommandLineInterface(application = "import-vcf")
public interface ImportVCFCommandOptions extends HelpOption {

  @Option(shortName = "n", description = "The VCF file name.")
  String getName();

  @Option(shortName = "p", description = "The project associated to the VCF store.")
  String getProject();

  @Option(shortName = "f", description = "The VCF file location.")
  String getFile();
}