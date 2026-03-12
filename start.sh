#!/bin/zsh
# GiroTech - Mata o processo na porta 8080 e reinicia a aplicação
echo "🔄 Verificando porta 8080..."
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "✅ Processo anterior encerrado" || echo "ℹ️  Porta 8080 já estava livre"
sleep 1
echo "🚀 Iniciando GiroTech..."
cd "/Users/camilaborgesoliveira/ Projects/projeto_integrado_girotech/girotech" && ./mvnw spring-boot:run

