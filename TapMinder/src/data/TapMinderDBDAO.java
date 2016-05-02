package data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import entities.Beer;
import entities.BeerRating;
import entities.Brewery;
import entities.Neighborhood;
import entities.User;
import entityData.State;

@Transactional
public class TapMinderDBDAO implements TapMinderDAO {

	@PersistenceContext
	private EntityManager em;

	public List<Beer> getBeers(BeerParameters beerParameters) {

		List<Beer> beerList = null;

		if (beerParameters.getName() != null) {
			beerList = getBeerListByName(beerParameters);
		} else if (beerParameters.getAbvHigh() != null && beerParameters.getAbvLow() != null) {
			beerList = getBeerListByABVRange(beerParameters);
		} else if (beerParameters.getAbvLow() != null && beerParameters.getAbvHigh() == null) {
			// search for higher abv than ^
			beerList = getBeerListByABVAbove(beerParameters);
		} else if (beerParameters.getAbvHigh() != null && beerParameters.getAbvLow() == null) {
			// search for lower abv than ^
			beerList = getBeerListByABVBelow(beerParameters);
		} else if (beerParameters.getStyle() != null) {
			beerList = getBeerListByStyle(beerParameters);
		} else if (beerParameters.getRatingLow() != null && beerParameters.getRatingHigh() != null) {
			beerList = getBeerListByRatingRange(beerParameters);

		} else if (beerParameters.getRatingLow() != null && beerParameters.getRatingHigh() == null) {
			beerList = getBeerListByRatingAbove(beerParameters);

		} else if (beerParameters.getRatingLow() == null && beerParameters.getRatingHigh() != null) {
			beerList = getBeerListByRatingBelow(beerParameters);

		} else if (beerParameters.getHopCountLow() != null && beerParameters.getHopCountHigh() != null) {
			// range
			beerList = getBeerListByHopCountRange(beerParameters);
		} else if (beerParameters.getHopCountLow() == null && beerParameters.getHopCountHigh() != null) {
			// above ^
			beerList = getBeerlistByHopCountLow(beerParameters);
		} else if (beerParameters.getHopCountLow() != null && beerParameters.getHopCountHigh() == null) {
			// below ^
			beerList = getBeerlistByHopCountHigh(beerParameters);
		}

		for (Beer beer : beerList) {
			em.detach(beer);
		}
		
		return beerList;

	}

	private List<Beer> getBeerListByName(BeerParameters beerParameters) {

		String name = beerParameters.getName().trim();

		name = "%" + name + "%";

		List<Beer> beerList = em.createQuery("SELECT b FROM Beer b WHERE b.name LIKE LOWER(:name) ", Beer.class)
				.setParameter("name", name.toLowerCase()).getResultList();

		return beerList;
	}

	private List<Beer> getBeerListByABVRange(BeerParameters beerParameters) {
		double low = beerParameters.getAbvLow();
		double high = beerParameters.getAbvHigh();

		String query = ("SELECT b FROM Beer b WHERE (b.abv >= :low) AND (b.abv <= :high) ORDER BY abv DESC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("low", low).setParameter("high", high)
				.getResultList();

		return beerList;
	}

	private List<Beer> getBeerListByABVAbove(BeerParameters beerParameters) {
		double higher = beerParameters.getAbvLow();
		String query = ("SELECT b FROM Beer b WHERE (b.abv >= :low) ORDER BY abv ASC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("low", higher).getResultList();
		return beerList;

	}

	private List<Beer> getBeerListByABVBelow(BeerParameters beerParameters) {
		double lower = beerParameters.getAbvHigh();
		String query = ("SELECT b FROM Beer b WHERE (b.abv <= :high) ORDER BY abv DESC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("high", lower).getResultList();
		return beerList;
	}

	private List<Beer> getBeerListByStyle(BeerParameters beerParameters) {
		String style = beerParameters.getStyle().trim();
		style = "%" + style + "%";
		List<Beer> beerList = em.createQuery("SELECT b FROM Beer b WHERE b.style LIKE LOWER(:style) ", Beer.class)
				.setParameter("style", style.toLowerCase()).getResultList();

		return beerList;
	}

	private List<Beer> getBeerListByRatingRange(BeerParameters beerParameters) {

		int ratingHigh = beerParameters.getRatingHigh();
		int ratingLow = beerParameters.getRatingLow();
		List<BeerRating> ratings = em
				.createQuery("SELECT r FROM BeerRating r WHERE r.rating <= :high AND r.rating >= :low",
						BeerRating.class)
				.setParameter("high", ratingHigh).setParameter("low", ratingLow).getResultList();
		Set<Beer> beerSet = new HashSet<>();

		for (BeerRating r : ratings) {
			beerSet.add(r.getBeer());
		}
		List<Beer> beerList = new ArrayList<>();
		beerList.addAll(beerSet);

		for (Beer beer : beerList) {
			System.out.println(beer.getName());
		}
		return beerList;

	}

	private List<Beer> getBeerListByRatingAbove(BeerParameters beerParameters) {
		//TODO untested
		int ratingLow = beerParameters.getRatingLow();
		
		List<BeerRating> ratings = em.createQuery("SELECT r FROM BeerRating r WHERE r.rating >= :low", BeerRating.class)
				.setParameter("low", ratingLow).getResultList();
		Set<Beer> beerSet = new HashSet<>();

		for (BeerRating r : ratings) {
			beerSet.add(r.getBeer());
		}
		List<Beer> beerList = new ArrayList<>();
		beerList.addAll(beerSet);
		
		return beerList;

	}

	 private List<Beer> getBeerListByRatingBelow(BeerParameters beerParameters){
	//TODO untested
	int ratingHigh = beerParameters.getRatingHigh();
		
	List<BeerRating> ratings = em.createQuery("SELECT r FROM BeerRating r WHERE r.rating <= :high",BeerRating.class).setParameter("high",ratingHigh).getResultList();
	Set<Beer> beerSet = new HashSet<>();

	for (BeerRating r : ratings) {
		beerSet.add(r.getBeer());
	}
	List<Beer> beerList = new ArrayList<>();
	beerList.addAll(beerSet);
	
	return beerList;
	
	 }
	 
	 
	private List<Beer> getBeerListByHopCountRange(BeerParameters beerParameters) {
		double low = beerParameters.getHopCountLow();
		double high = beerParameters.getHopCountHigh();

		String query = ("SELECT b FROM Beer b WHERE (b.hopCount >= :low) AND (b.hopCount <= :high) ORDER BY hopCount DESC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("low", low).setParameter("high", high)
				.getResultList();

		return beerList;
	}

	private List<Beer> getBeerlistByHopCountLow(BeerParameters beerParameters) {
		double lower = beerParameters.getHopCountHigh();
		String query = ("SELECT b FROM Beer b WHERE (b.hopCount <= :high) ORDER BY hopCount DESC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("high", lower).getResultList();

		return beerList;
	}

	private List<Beer> getBeerlistByHopCountHigh(BeerParameters beerParameters) {
		double higher = beerParameters.getHopCountLow();
		String query = ("SELECT b FROM Beer b WHERE (b.hopCount >= :low) ORDER BY hopCount DESC");
		List<Beer> beerList = em.createQuery(query, Beer.class).setParameter("low", higher).getResultList();
		return beerList;
	}

	@Override
	public void modifyBeer(Beer beer) {
		em.persist(beer);
	}

	@Override
	public void deleteBeer(Beer beer) {
		em.remove(beer);
	}

	@Override
	public List<Brewery> getBreweries(BreweryParameters breweryParameters) {
		List<Brewery> breweryList = null;
		if(breweryParameters.getName() != null){
			breweryList = getBreweryListByName(breweryParameters);
		}
		else if(breweryParameters.getStreetAddress() != null){
			breweryList = getBreweryListByStreetAddress(breweryParameters);
		}
		else if(breweryParameters.getCity() != null){
			breweryList = getBreweryListByCity(breweryParameters);
		}
		else if(breweryParameters.getState() != null){
			breweryList = getBreweryListByState(breweryParameters);
		}
		else if(breweryParameters.getZipcode() != null){
			breweryList = getBreweryListByZipcode(breweryParameters);
		}
		else if(breweryParameters.getNeighborhood() != null){
		breweryList = getBreweryListByNeighborhood(breweryParameters);	
		}
		//TODO: put this in the other things
		for (Brewery brewery : breweryList) {
			em.detach(brewery);
		}
		return breweryList;
	}
	
	private List<Brewery> getBreweryListByName(BreweryParameters breweryParameters){
		String name = "%" + breweryParameters.getName() + "%";
		String query = "SELECT b FROM Brewery b WHERE LOWER(b.name) LIKE :name";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("name", name.toLowerCase()).getResultList();
		return breweryList;
		
	}
	private List<Brewery> getBreweryListByStreetAddress(BreweryParameters breweryParameters){
		String streetAddress = "%" + breweryParameters.getStreetAddress() + "%";
		String query = "SELECT b FROM Brewery b WHERE LOWER(b.streetAddress) LIKE :streetAddress";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("streetAddress", streetAddress.toLowerCase()).getResultList();
		return breweryList;
	}
	private List<Brewery> getBreweryListByCity(BreweryParameters breweryParameters){
		String city = "%" + breweryParameters.getCity() + "%";
		String query = "SELECT b FROM Brewery b WHERE LOWER(b.city) LIKE :city";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("city", city.toLowerCase()).getResultList();
		return breweryList;
	}
	private List<Brewery> getBreweryListByState(BreweryParameters breweryParameters){
		State state = breweryParameters.getState();
		String query = "SELECT b FROM Brewery b WHERE b.state =  :state";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("state", state).getResultList();
		return breweryList;
	}
	private List<Brewery> getBreweryListByZipcode(BreweryParameters breweryParameters){
		Integer zip = 80226;
		String query = "SELECT b FROM Brewery b WHERE b.zip = :zip";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("zip", zip).getResultList();
		
		return breweryList;
		
		
	}
	private List<Brewery> getBreweryListByNeighborhood(BreweryParameters breweryParameters){
		int neighborhoodId = breweryParameters.getNeighborhood().getId();
		String query = "SELECT b FROM Brewery b WHERE b.neighborhood.id = :neighborhoodId";
		
		List<Brewery> breweryList = em.createQuery(query,Brewery.class).setParameter("neighborhoodId", neighborhoodId).getResultList();
		return breweryList;
	}


	@Override
	public void modifyBrewery(Brewery brewery) {
		em.persist(brewery);
	}

	@Override
	public void deleteBrewery(Brewery brewery) {
		em.remove(brewery);
	
	}

	@Override
	public List<User> getUserList() {
		List<User> userList=em.createQuery("SELECT u FROM User u", User.class).getResultList();
		return userList;
	}

	@Override
	public User getUser(int userId) {
		
		String query = "SELECT u FROM User u WHERE u.id = :userId" ;
		User userList =em.createQuery(query, User.class).setParameter("userId", userId).getSingleResult();
		return userList;
	}

	@Override
	public LoginResult getUserByLoginCredentials(User userToLogin){
		LoginResult lr = null;
		try{
		String query = "SELECT u FROM User u WHERE u.email = :email AND u.password = :password";
		User user = em.createQuery(query,User.class).setParameter("email",userToLogin.getEmail()).setParameter("password", userToLogin.getPassword()).getSingleResult();
		//TODO		
		System.out.println("LOGGED IN USER " + user);
		lr = new LoginResult(user,"");
		}catch(NoResultException e){
		lr = new LoginResult(null,"Incorrect Username Or Password");
		}
		
		
		return lr;
	}
	
	@Override
	public void modifyUser(User user) {
		em.persist(user);
	}

	@Override
	public void deleteUser(User user) {
		em.remove(user);
	}

	@Override
	public List<BeerRating> getRatingsByUser(User user) {

		List<BeerRating> ratingList = user.getRatings();
		
		return ratingList;
	}

	@Override
	public List<BeerRating> getRatingsByBeer(Beer beer) {
		//TODO untested
		
		List<BeerRating> ratingList = beer.getRatings();
		
		return ratingList;
	}

	@Override
	public List<BeerRating> getRatingsByBrewery(Brewery brewery) {
		List<Beer> beerList = brewery.getBeerList();
		
		List<BeerRating> ratingList = new ArrayList<>();
		
		for (Beer beer : beerList) {
			ratingList.addAll(beer.getRatings());
		}
		
		return ratingList;
	}

	@Override
	public void modifyRating(BeerRating beerRating) {
		em.persist(beerRating);
	}

	@Override
	public void deleteRating(BeerRating beerRating) {
		em.remove(beerRating);
	}

	@Override
	public List<Neighborhood> getNeighborhoods() {
		
		List<Neighborhood> neighborhoods = em.createQuery("Select n from Neighborhood n",Neighborhood.class).getResultList();
		
		for (Neighborhood neighborhood : neighborhoods) {
			em.detach(neighborhood);
		}
		
		return null;

	}

}
