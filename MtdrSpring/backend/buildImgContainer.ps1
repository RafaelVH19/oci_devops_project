docker stop agilecontainer
docker rm -f agilecontainer
docker rmi agileimage
mvn clean verify
docker build -f DockerfileDev --platform linux/amd64 -t agileimage:0.1 .
# Resend: baked via application-local.properties on mvn package, or add -e RESEND_API_KEY=... locally
docker run --name agilecontainer -p 8080:8080 -d `
  -e AUTH_SERVER_URL=http://host.docker.internal:3001 `
  -e INVITE_API_SECRET=dev-invite-secret-change-me `
  agileimage:0.1
