curl -i http://localhost:8080/projects
curl -i -X POST http://localhost:8080/projects -H "Content-Type: application/json" -d "{\"name\":\"Example\",\"description\":\"desc\",\"details\":\"initial details\"}"
curl -i http://localhost:8080/projects
curl -i -X PUT http://localhost:8080/projects/1 -H "Content-Type: application/json" -d "{\"name\":\"Updated\",\"description\":\"new\",\"details\":\"updated details\"}"
curl -i http://localhost:8080/projects
curl -i -X DELETE http://localhost:8080/projects/1
curl -i http://localhost:8080/projects

