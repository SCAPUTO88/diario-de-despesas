# Sistema de Gerenciamento de Despesas

Um sistema robusto para gerenciamento de despesas pessoais, construído com Spring Boot e DynamoDB.

## 🚀 Tecnologias Utilizadas

- Java 21
- Spring Boot
- AWS DynamoDB
- Spring Security
- Micrometer (Monitoramento)
- Docker
- JUnit 5 & Mockito

## 📋 Pré-requisitos

- Java 21
- Docker e Docker Compose
- Maven
- AWS CLI (opcional)

## 🔧 Instalação e Execução

1. **Clone o repositório:**
   bash git clone [url-do-repositorio] cd despesas-projeto

2. **Inicie o DynamoDB local:**
   bash docker-compose up -d

3. **Execute a aplicação:**
```bash
./mvnw spring-boot:run
```
A aplicação estará disponível em `http://localhost:8080`
## 🏗️ Arquitetura
### Estrutura do Projeto
```bash
src/
├── main/
│   ├── java/
│   │   └── com/example/despesas_projeto/
│   │       ├── config/        # Configurações
│   │       ├── controller/    # Controllers REST
│   │       ├── domain/       # Objetos de domínio
│   │       ├── exception/    # Exceções customizadas
│   │       ├── model/        # Entidades
│   │       ├── repository/   # Repositórios
│   │       └── service/      # Regras de negócio
│   └── resources/
└── test/
└── java/
└── com/example/despesas_projeto/
```
### Decisões Técnicas
1. **DynamoDB**
    - Escolhido pela escalabilidade e performance
    - Modelagem NoSQL otimizada para consultas frequentes

2. **Segurança**
    - Autenticação JWT
    - Perfis de usuário
    - Criptografia de senhas com BCrypt

3. **Monitoramento**
    - Métricas com Micrometer
    - Health checks personalizados
    - Logs estruturados em JSON


## 📌 API Endpoints
### Autenticação
``` http
POST /api/v1/auth/register
POST /api/v1/auth/authenticate
```
### Transações
``` http
POST   /api/v1/transactions            # Criar transação
GET    /api/v1/transactions/{id}       # Buscar transação
PUT    /api/v1/transactions/{id}       # Atualizar transação
DELETE /api/v1/transactions/{id}       # Deletar transação
GET    /api/v1/transactions           # Listar transações
```
### Exemplos de Uso
#### Criando uma nova transação
``` bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu-token" \
  -d '{
    "description": "Supermercado",
    "value": 150.00,
    "type": "EXPENSE",
    "category": "Alimentação",
    "transactionDate": "2024-01-20T10:00:00"
  }'
```
#### Consultando o saldo mensal
``` bash
curl http://localhost:8080/api/v1/transactions/balance/2024/1 \
  -H "Authorization: Bearer seu-token"
```
## 🔍 Monitoramento
### Endpoints do Actuator
- `/actuator/health` - Status da aplicação
- `/actuator/metrics` - Métricas
- `/actuator/prometheus` - Métricas formato Prometheus

### Métricas Disponíveis
- `transactions_total` - Total de transações processadas
- `transactions_by_type` - Transações por tipo
- `transactions_errors` - Erros no processamento

## 🧪 Testes
Execute os testes com:
``` bash
./mvnw test
```
Para testes de integração:
``` bash
./mvnw verify
```
## 📦 Build e Deploy
1. **Build do projeto:**
``` bash
./mvnw clean package
```
1. **Build da imagem Docker:**
``` bash
docker build -t despesas-app .
```
1. **Deploy local:**
``` bash
docker-compose -f docker-compose.prod.yml up -d
```

## ✒️ Autores
- **Sandro Nogueira Caputo** - _Desenvolvimento inicial_
