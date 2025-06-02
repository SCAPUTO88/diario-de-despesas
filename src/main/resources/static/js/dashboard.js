
document.addEventListener('DOMContentLoaded', function() {
console.log('Dados Evolução Mensal:', dadosEvolucaoMensal);
    console.log('Dados Categorias:', dadosCategorias);

    // Configuração de cores
    const colors = {
        receitas: 'rgba(40, 167, 69, 0.7)',    // verde
        despesas: 'rgba(220, 53, 69, 0.7)',    // vermelho
        categorias: [
            'rgba(255, 99, 132, 0.7)',
            'rgba(54, 162, 235, 0.7)',
            'rgba(255, 206, 86, 0.7)',
            'rgba(75, 192, 192, 0.7)',
            'rgba(153, 102, 255, 0.7)',
            'rgba(255, 159, 64, 0.7)'
        ]
    };

    // Gráfico de Evolução Mensal
const evolucaoMensalCtx = document.getElementById('evolucaoMensalChart').getContext('2d');
new Chart(evolucaoMensalCtx, {
    type: 'line',
    data: {
        labels: dadosEvolucaoMensal.labels,
        datasets: [{
            label: 'Receitas',
            data: dadosEvolucaoMensal.receitas,
            borderColor: colors.receitas,
            backgroundColor: colors.receitas,
            tension: 0.4,
            fill: false
        }, {
            label: 'Despesas',
            data: dadosEvolucaoMensal.despesas,
            borderColor: colors.despesas,
            backgroundColor: colors.despesas,
            tension: 0.4,
            fill: false
        }]
    },

        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                title: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL'
                            }).format(value);
                        }
                    }
                }
            }
        }
    });

    // Gráfico de Categorias
    const categoriasCtx = document.getElementById('categoriasChart').getContext('2d');
    const categorias = Object.keys(dadosCategorias);
    const valoresCategorias = Object.values(dadosCategorias);

    new Chart(categoriasCtx, {
        type: 'doughnut',
        data: {
            labels: categorias,
            datasets: [{
                data: valoresCategorias,
                backgroundColor: colors.categorias,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.raw;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value * 100) / total).toFixed(1);
                            return `${context.label}: ${new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL'
                            }).format(value)} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });

    // Listener para mudança de período
    document.getElementById('evolucaoMensalPeriodo').addEventListener('change', function(e) {
        const meses = parseInt(e.target.value);
    });

});