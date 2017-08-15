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

@Api(name = "restauranteendpoint", namespace = @ApiNamespace(ownerDomain = "boteco.comida", ownerName = "boteco.comida", packagePath = "server"))
public class RestauranteEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listRestaurante")
	public CollectionResponse<Restaurante> listRestaurante(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Restaurante> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Restaurante as Restaurante");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Restaurante>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Restaurante obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Restaurante>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getRestaurante")
	public Restaurante getRestaurante(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		Restaurante restaurante = null;
		try {
			restaurante = mgr.find(Restaurante.class, id);
		} finally {
			mgr.close();
		}
		return restaurante;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param restaurante the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertRestaurante")
	public Restaurante insertRestaurante(Restaurante restaurante) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsRestaurante(restaurante)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(restaurante);
		} finally {
			mgr.close();
		}
		return restaurante;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param restaurante the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateRestaurante")
	public Restaurante updateRestaurante(Restaurante restaurante) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsRestaurante(restaurante)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(restaurante);
		} finally {
			mgr.close();
		}
		return restaurante;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeRestaurante")
	public void removeRestaurante(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		try {
			Restaurante restaurante = mgr.find(Restaurante.class, id);
			mgr.remove(restaurante);
		} finally {
			mgr.close();
		}
	}

	private boolean containsRestaurante(Restaurante restaurante) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Restaurante item = mgr.find(Restaurante.class, restaurante.getId());
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
