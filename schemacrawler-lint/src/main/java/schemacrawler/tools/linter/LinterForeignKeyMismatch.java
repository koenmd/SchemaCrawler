/*
 *
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2015, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */
package schemacrawler.tools.linter;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;
import schemacrawler.tools.lint.BaseLinter;
import schemacrawler.tools.lint.LintSeverity;

public class LinterForeignKeyMismatch
  extends BaseLinter
{

  public LinterForeignKeyMismatch()
  {
    setSeverity(LintSeverity.high);
  }

  @Override
  public String getSummary()
  {
    return "foreign key data type different from primary key";
  }

  @Override
  protected void lint(final Table table, final Connection connections)
  {
    requireNonNull(table, "No table provided");

    final List<ForeignKey> mismatchedForeignKeys = findMismatchedForeignKeys(table);
    for (final ForeignKey foreignKey: mismatchedForeignKeys)
    {
      addTableLint(table, getSummary(), foreignKey);
    }
  }

  private List<ForeignKey> findMismatchedForeignKeys(final Table table)
  {
    final List<ForeignKey> mismatchedForeignKeys = new ArrayList<>();
    if (table != null && !(table instanceof View))
    {
      for (final ForeignKey foreignKey: table.getImportedForeignKeys())
      {
        for (final ForeignKeyColumnReference columnReference: foreignKey)
        {
          final Column pkColumn = columnReference.getPrimaryKeyColumn();
          final Column fkColumn = columnReference.getForeignKeyColumn();
          if (!pkColumn.getColumnDataType().equals(fkColumn.getColumnDataType())
              || pkColumn.getSize() != fkColumn.getSize())
          {
            mismatchedForeignKeys.add(foreignKey);
            break;
          }
        }
      }
    }
    return mismatchedForeignKeys;
  }
}
