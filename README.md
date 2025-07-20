# M3Gestor - Sistema de Gerenciamento para Madeireira

Sistema desktop para gerenciamento de clientes, cálculo de metro cúbico de madeira e geração de orçamentos para Madeireira Pai e Filhos.

## Funcionalidades

- **Cadastro de Clientes**: Gerencie os dados dos clientes, incluindo nome, endereço, telefone, email e CPF/CNPJ.
- **Cálculo de Metro Cúbico**: Calcule automaticamente o volume de madeira em metros cúbicos com base nas dimensões fornecidas.
- **Orçamentos**: Crie e gerencie orçamentos detalhados com itens, quantidades e valores unitários.
- **Interface Intuitiva**: Design moderno e fácil de usar.

## Requisitos do Sistema

- Java 11 ou superior
- Maven 3.6 ou superior
- JavaFX 17 ou superior

## Como Executar o Projeto

### Configuração Inicial

1. Certifique-se de ter o Java JDK 11+ e o Maven instalados.
2. Clone o repositório:
   ```bash
   git clone [URL_DO_REPOSITORIO]
   cd M3Gestor
   ```

### Executando com Maven

```bash
mvn clean javafx:run
```

### Empacotando a Aplicação

Para criar um pacote executável:

```bash
mvn clean package
```

O arquivo JAR será gerado no diretório `target/`.

## Estrutura do Projeto

```
src/main/java/com/caua/madeira/
├── Main.java               # Classe principal da aplicação
├── model/                  # Classes de modelo
│   └── QuoteItem.java      # Item do orçamento
├── view/                   # Componentes de interface do usuário
│   ├── ClientForm.java     # Formulário de cadastro de clientes
│   ├── ClientList.java     # Lista de clientes cadastrados
│   ├── QuoteForm.java      # Formulário de orçamento
│   └── QuoteList.java      # Lista de orçamentos
└── util/                  # Utilitários
    └── RelatorioUtil.java  # Geração de relatórios (futura implementação)
```

## Banco de Dados

O sistema utiliza um banco de dados PostgreSQL. Certifique-se de ter o PostgreSQL instalado e execute o script SQL localizado em `src/main/resources/database/schema.sql` para criar as tabelas necessárias.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para obter mais informações.

## Contato

Para mais informações, entre em contato com a equipe de desenvolvimento.
