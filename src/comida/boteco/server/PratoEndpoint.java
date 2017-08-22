package comida.boteco.server;

import comida.boteco.EMF;

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

@Api(name = "pratoendpoint", namespace = @ApiNamespace(ownerDomain = "boteco.comida", ownerName = "boteco.comida", packagePath = "server"))
public class PratoEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPrato")
	public CollectionResponse<Prato> listPrato(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Prato> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Prato as Prato");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Prato>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Prato obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Prato>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPrato")
	public Prato getPrato(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		Prato prato = null;
		try {
			prato = mgr.find(Prato.class, id);
		} finally {
			mgr.close();
		}
		return prato;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param prato the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPrato")
	public Prato insertPrato(Prato prato) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsPrato(prato)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(prato);
		} finally {
			mgr.close();
		}
		return prato;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param prato the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePrato")
	public Prato updatePrato(Prato prato) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsPrato(prato)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(prato);
		} finally {
			mgr.close();
		}
		return prato;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePrato")
	public void removePrato(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		try {
			Prato prato = mgr.find(Prato.class, id);
			mgr.remove(prato);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPrato(Prato prato) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Prato item = mgr.find(Prato.class, prato.getId());
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
