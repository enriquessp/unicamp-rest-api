package br.com.boteco.server;

import br.com.boteco.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "pratosendpoint", namespace = @ApiNamespace(ownerDomain = "com.br", ownerName = "com.br", packagePath = "boteco.server"))
public class PratosEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPratos")
	public CollectionResponse<Pratos> listPratos(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Pratos> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Pratos as Pratos");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Pratos>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Pratos obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Pratos>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPratos")
	public Pratos getPratos(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		Pratos pratos = null;
		try {
			pratos = mgr.find(Pratos.class, id);
		} finally {
			mgr.close();
		}
		return pratos;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param pratos the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPratos")
	public Pratos insertPratos(Pratos pratos) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsPratos(pratos)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(pratos);
		} finally {
			mgr.close();
		}
		return pratos;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param pratos the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePratos")
	public Pratos updatePratos(Pratos pratos) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsPratos(pratos)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(pratos);
		} finally {
			mgr.close();
		}
		return pratos;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePratos")
	public void removePratos(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		try {
			Pratos pratos = mgr.find(Pratos.class, id);
			mgr.remove(pratos);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPratos(Pratos pratos) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Pratos item = mgr.find(Pratos.class, pratos.getId());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
