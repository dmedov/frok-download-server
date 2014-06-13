frok-download-server
====================

Путь к запуску:

Качаете IDEA 13.0.2 Ultimane trial http://www.jetbrains.com/idea/download/

Качаете Glassfish сервер https://glassfish.java.net/download.html

Импортируете репозиторий в IDEA, выбираем Edit Configuration.

1) Application server - указываем путь до папки ./glassfish

Open Browser: Указываем что-то вроде http://localhost:8080/frok/

Server Domain: указываем путь до папки с domain, ищите там, где glassfish. У меня это C:\Program Files (x86)\glassfish\glassfish\domains\domain1

Username = admin Password = admin

2) Заходим в Deployments, добавляем frok:war

3) Ставим галку "use custom context root" и вписываем "/frok"
