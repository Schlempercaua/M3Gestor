@echo off
echo Iniciando M3Gestor...

REM Verifica se o Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Erro: Java nao encontrado. Por favor, instale o Java 17 ou superior.
    pause
    exit /b 1
)

REM Tenta executar com o JavaFX Plugin do Maven primeiro
echo Tentando executar com Maven...
call mvn javafx:run
if %errorlevel% equ 0 (
    echo Aplicacao executada com sucesso!
    pause
    exit /b 0
)

REM Se falhar, tenta executar com o JAR diretamente
echo Maven falhou, tentando executar JAR diretamente...
java -jar target/m3gestor-1.0.jar
if %errorlevel% neq 0 (
    echo Erro ao executar a aplicacao.
    echo Verifique se o JavaFX esta instalado ou se o banco de dados PostgreSQL esta rodando.
    pause
    exit /b 1
)

pause
