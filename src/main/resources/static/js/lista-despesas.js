document.addEventListener('DOMContentLoaded', function() {
    // Gerenciamento de ordenação
    const form = document.querySelector('form');
    const sortButtons = document.querySelectorAll('button[data-sort]');

    if (form && sortButtons) {
        sortButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                // Previne qualquer comportamento padrão
                e.preventDefault();
                e.stopPropagation();

                // Obtém os campos de ordenação
                const ordenacaoInput = form.querySelector('[name="filtro.ordenacao"]');
                const direcaoInput = form.querySelector('[name="filtro.direcao"]');
                const paginaInput = form.querySelector('[name="filtro.pagina"]');

                if (!ordenacaoInput || !direcaoInput || !paginaInput) {
                    console.error('Campos de ordenação não encontrados');
                    return;
                }

                // Obtém o novo campo de ordenação
                const novoField = this.getAttribute('data-sort');
                const mesmoField = ordenacaoInput.value === novoField;

                // Atualiza os campos
                ordenacaoInput.value = novoField;
                if (mesmoField) {
                    direcaoInput.value = direcaoInput.value === 'asc' ? 'desc' : 'asc';
                } else {
                    direcaoInput.value = 'asc';
                }
                paginaInput.value = '0';

                // Cria um evento de submit personalizado
                const submitEvent = new Event('submit', {
                    bubbles: true,
                    cancelable: true
                });

                // Dispara o evento
                form.dispatchEvent(submitEvent);
            });
        });

        // Previne o comportamento padrão do formulário
        form.addEventListener('submit', function(e) {
            // Remove o hash da URL se existir
            if (window.location.hash) {
                window.history.pushState('', document.title, window.location.pathname + window.location.search);
            }
        });
    }
});
