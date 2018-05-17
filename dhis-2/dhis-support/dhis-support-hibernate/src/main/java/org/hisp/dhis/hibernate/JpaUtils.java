package org.hisp.dhis.hibernate;

/*
 *
 *  Copyright (c) 2004-2018, University of Oslo
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Viet Nguyen <viet@dhis2.org>
 */
public class JpaUtils
{
    public static final String HIBERNATE_CACHEABLE_HINT = "org.hibernate.cacheable";

    public static Function<Root<?>, Order> getOrders( CriteriaBuilder builder, String field )
    {
        Function<Root<?>, Order> order = root -> builder.asc( root.get( field ) );

        return order;
    }


    public static Predicate andPredicate( CriteriaBuilder buidler, Predicate... predicates )
    {
        List<Predicate> predicateList = Lists.newArrayList( predicates );

        if ( predicateList == null || predicateList.isEmpty() )
        {
            return null;
        }

        if ( predicateList.size() == 0 )
        {
            return predicateList.get( 0 );
        }

        return buidler.and( Iterables.toArray( predicateList, Predicate.class ) );
    }

    /**
     *
     * @param builder CriteriaBuilder
     * @param path Property Path for query
     * @param attrValue Value to check
     * @param searchMode JpaUtils.StringSearchMode
     * @param caseSesnitive is case sensitive
     * @return
     */
    public static Predicate stringPredicate( CriteriaBuilder builder, Expression<String> path, Object attrValue, StringSearchMode searchMode, boolean caseSesnitive )
    {
        if ( !caseSesnitive )
        {
            path = builder.lower( path );
            attrValue = ((String) attrValue).toLowerCase( LocaleContextHolder.getLocale() );
        }

        switch ( searchMode )
        {
            case EQUALS:
                return builder.equal( path, attrValue );
            case ENDING_LIKE:
                return builder.like( path, "%" + attrValue );
            case STARTING_LIKE:
                return builder.like( path, attrValue + "%" );
            case ANYWHERE:
                return builder.like( path, "%" + attrValue + "%" );
            case LIKE:
                return builder.like( path, (String) attrValue ); // assume user provide the wild cards
            default:
                throw new IllegalStateException( "expecting a search mode!" );
        }
    }

    public static <T> Path getIdAttribute( Root<T> root, String attributeName ) throws NoSuchFieldException
    {
        EntityType entity = root.getModel();

        Set<SingularAttribute<? super T, ?>> attributes = entity.getIdClassAttributes();

        SingularAttribute<? super T, ?> attr = attributes.stream().filter( a -> a.getName().equals( attributeName ) ).findFirst().orElse( null );

        return attr != null ? root.get( attr ) : null;
    }

    public enum StringSearchMode
    {
        // Match exactly
        EQUALS( "eq" ),

        // Like search with '%' prefix and suffix
        ANYWHERE( "any" ),

        // Like search and add a '%' prefix before searching.
        STARTING_LIKE( "sl" ),

        // User provides the wildcard.
        LIKE( "li" ),

        // LIKE search and add a '%' suffix before searching.
        ENDING_LIKE( "el" );

        private final String code;

        StringSearchMode( String code )
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static final StringSearchMode convert( String code )
        {
            for ( StringSearchMode searchMode : StringSearchMode.values() )
            {
                if ( searchMode.getCode().equals( code ) )
                {
                    return searchMode;
                }
            }

            return EQUALS;
        }
    }
}
