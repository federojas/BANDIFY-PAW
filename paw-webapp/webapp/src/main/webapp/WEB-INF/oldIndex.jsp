<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<html>
<body>
<h2>Hello <c:out value="${user.username}"/>! </h2>
<h4>The userId is <c:out value="${user.id}"/>! </h4>
</body>
</html>