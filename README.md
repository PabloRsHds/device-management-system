# Device Management System - Backend

Sistema de gerenciamento de dispositivos IoT com arquitetura de microserviços.

## Arquitetura

- **Device Service**: CRUD de dispositivos, comunicação via Feign
- **Sensor Test Service**: Testes agendados de sensores, envio para Kafka
- **IoT Gateway**: Validações brutas dos dados
- **Sensor Analysis**: Análises detalhadas dos dados
- **Apache Kafka**: Comunicação assíncrona entre serviços

## Tecnologias

- Java
- Spring Boot
- Spring Kafka
- Feign Client
- Spring Scheduling
- PostgreSQL
- Docker

## A adicionar

- Microserviço para login
- Implementar segurança
- Implementar validações

## Como Executar

```bash
# Clone o repositório
git clone https://github.com/PabloRsHds/device-management-system.git
cd device-management-system

# Execute com Docker Compose
docker-compose up -d
```