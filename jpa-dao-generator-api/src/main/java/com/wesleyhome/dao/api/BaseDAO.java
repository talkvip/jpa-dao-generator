/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;

public abstract class BaseDAO<E> extends BaseObjectDAO {

    protected <K extends Serializable> boolean exists(final Class<E> entityClass, final SingularAttribute<? super E, K> idAttribute,
                                                      final K value) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> root = cq.from(entityClass);
        Path<K> idPath = root.get(idAttribute);
        cq.select(cb.count(idPath)).where(equals(idPath, value));
        return getSingleResult(cq).longValue() > 0;
    }

    protected Object getKey(final E entity) {
        PersistenceUnitUtil pu = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        Object identifier = pu.getIdentifier(entity);
        if (identifier instanceof String) {
            identifier = ((String) identifier).trim();
        }
        return identifier;
    }
}
