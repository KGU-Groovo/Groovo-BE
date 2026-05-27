COMPOSE := docker compose
APP_SERVICE := app
MYSQL_SERVICE := mysql
REDIS_SERVICE := redis

.PHONY: all up build down re clean fclean restart logs app-logs db-logs redis-logs ps config health app-shell db-shell redis-cli seed

all: up

up:
	$(COMPOSE) up --build -d

build:
	$(COMPOSE) build

down:
	$(COMPOSE) down

re: fclean all

clean:
	$(COMPOSE) down --remove-orphans

fclean:
	$(COMPOSE) down --volumes --rmi local --remove-orphans

restart:
	$(COMPOSE) restart

logs:
	$(COMPOSE) logs -f --tail=200

app-logs:
	$(COMPOSE) logs -f --tail=200 $(APP_SERVICE)

db-logs:
	$(COMPOSE) logs -f --tail=200 $(MYSQL_SERVICE)

redis-logs:
	$(COMPOSE) logs -f --tail=200 $(REDIS_SERVICE)

ps:
	$(COMPOSE) ps

config:
	$(COMPOSE) config

health:
	$(COMPOSE) exec $(APP_SERVICE) wget -qO- http://localhost:$${SERVER_PORT:-8080}/actuator/health

app-shell:
	$(COMPOSE) exec $(APP_SERVICE) sh

db-shell:
	$(COMPOSE) exec $(MYSQL_SERVICE) sh -lc 'mysql -u"$$MYSQL_USER" -p"$$MYSQL_PASSWORD" "$$MYSQL_DATABASE"'

redis-cli:
	$(COMPOSE) exec $(REDIS_SERVICE) redis-cli

seed:
	$(COMPOSE) exec -T $(MYSQL_SERVICE) sh -lc 'exec mysql -u"$$MYSQL_USER" -p"$$MYSQL_PASSWORD" "$$MYSQL_DATABASE"' < scripts/seed_videos.sql
	@echo "Seeded demo videos."
