#!/bin/zsh
# GiroTech - Mata o processo na porta 8080 e reinicia a aplicação
echo "Verificando porta 8080..."
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "Processo anterior encerrado" || echo "Porta 8080 ja estava livre"
sleep 1

# Carrega variáveis de ambiente do .env (se existir)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
  echo "Carregando variaveis de ambiente do .env..."
  set -a
  source "$SCRIPT_DIR/.env"
  set +a
fi

echo "Iniciando GiroTech..."
cd "$SCRIPT_DIR" && ./mvnw spring-boot:run

