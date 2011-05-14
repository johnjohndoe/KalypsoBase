/*
 * $Id: SDOObjectProperty.java 268 2010-10-28 19:16:54Z maesenka $
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.oracle;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.util.List;

/**
 * Special function for accessing a member variable of an Oracle Object
 *
 * @author Karel Maesen
 */
class SDOObjectProperty implements SQLFunction {

    private final Type type;

    private final String name;

    public SDOObjectProperty(String name, Type type) {
        this.type = type;
        this.name = name;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.dialect.function.SQLFunction#getReturnType(org.hibernate.type.Type,
      *      org.hibernate.engine.Mapping)
      */

    public Type getReturnType(Type columnType, Mapping mapping)
            throws QueryException {
        return type == null ? columnType : type;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.dialect.function.SQLFunction#hasArguments()
      */

    public boolean hasArguments() {
        return true;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.dialect.function.SQLFunction#hasParenthesesIfNoArguments()
      */

    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.dialect.function.SQLFunction#render(java.util.List,
      *      org.hibernate.engine.SessionFactoryImplementor)
      */

    public String render(Type firstArgtype, List args, SessionFactoryImplementor factory)
            throws QueryException {
        StringBuffer buf = new StringBuffer();
        if (args.isEmpty())
            throw new QueryException(
                    "First Argument in arglist must be object of which property is queried");
        buf.append(args.get(0)).append(".").append(name);
        return buf.toString();
    }

}
