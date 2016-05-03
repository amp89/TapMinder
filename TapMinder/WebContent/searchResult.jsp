<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<c:forEach var="beer" items="${beerList}">
	Name: ${beer.name} <br>
	ABV: ${beer.abv} <br>
	HopCount: ${beer.hopCount} <br>
	Style: ${beer.beerStyle} <br>
	Brewery: ${beer.brewery.name} <br>
	Rating: ${beer.averageRating}<br/>
	<form action="rateABeer.do">
		<input type="hidden" name="beerId" value="${beer.id}" />
		<button type="submit">Rate This Beer</button>
	</form>
	<img src="${beer.imageLink}" />
	<br>
	
			
			<form action="modifyBeerPage.do">
				<input type="hidden" name="beerId" value="${beer.id}" />

				<button type="submit">Modify</button>
			</form>
			<form action="deleteBeer.do">
				<input type="hidden" name="beerId" value="${beer.id}" />

				<button type="submit">Delete</button>
			</form>
</c:forEach>


</body>
</html>