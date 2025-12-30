# Device Management System - Frontend

Frontend do sistema de gerenciamento de dispositivos IoT, desenvolvido em Angular/TypeScript.

## Funcionalidades Implementadas

- **Registro de dispositivos** - Formulário completo de cadastro
- **Listagem de dispositivos** - Tabela com paginação e filtros
- **Atualização de dispositivos** - Edição de informações
- **Exclusão de dispositivos** - Remoção com confirmação
- **Teste de sensores** - Interface para acionar testes
- **Visualização de sensores** - Dashboard com dados em tempo real

## Funcionalidades Futuras

- **Página de login** - Autenticação de usuários
- **Controle de acesso** - Permissões por perfil
- **Dashboard analítico** - Gráficos e métricas
- **Notificações** - Alertas em tempo real

## Tecnologias

- Angular 19+
- TypeScript
- RxJS para gerenciamento de estado
- Angular Material para UI components
- Chart.js (para futuros gráficos)
- Angular HttpClient para APIs

## Instalação

```bash
# Clone o repositório
git clone https://github.com/PabloRsHds/device-management-frontend

# Entre na pasta
cd device-management-frontend

# Instale dependências
npm install

# Execute em desenvolvimento
npm start
# ou
ng serve