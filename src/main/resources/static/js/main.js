document.addEventListener('DOMContentLoaded', function() {
    // Gerenciamento do gráfico
    const ctx = document.getElementById('despesasChart');
    if (ctx) {
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(despesasPorCategoria),
                datasets: [{
                    data: Object.values(despesasPorCategoria),
                    backgroundColor: [
                        '#2D4059',
                        '#EA5455',
                        '#F07B3F',
                        '#FFD460'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

   // Gerenciamento de ordenação
       const sortButtons = document.querySelectorAll('button[data-sort]');
       const form = document.querySelector('form');

       if (sortButtons && form) {
           sortButtons.forEach(button => {
               button.addEventListener('click', function(e) {
                   e.preventDefault();

                   const ordenacaoInput = form.querySelector('[name="filtro.ordenacao"]');
                   const direcaoInput = form.querySelector('[name="filtro.direcao"]');
                   const paginaInput = form.querySelector('[name="filtro.pagina"]');

                   if (!ordenacaoInput || !direcaoInput || !paginaInput) {
                       console.error('Campos de ordenação não encontrados');
                       return;
                   }

                   const novoField = this.getAttribute('data-sort');
                   const mesmoField = ordenacaoInput.value === novoField;

                   ordenacaoInput.value = novoField;
                   direcaoInput.value = mesmoField && direcaoInput.value === 'asc' ? 'desc' : 'asc';
                   paginaInput.value = '0';

                   try {
                       form.submit();
                   } catch (err) {
                       console.error('Erro ao submeter formulário:', err);
                   }
               });
           });
       }
   });

