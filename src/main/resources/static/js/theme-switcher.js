document.addEventListener('DOMContentLoaded', function() {
    // Carregar tema salvo ou usar padrão
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);

    // Atualizar estado do switch
    const themeSwitch = document.getElementById('themeSwitch');
    if (themeSwitch) {
        themeSwitch.checked = savedTheme === 'dark';
    }
});

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    // Aplicar novo tema
    document.documentElement.setAttribute('data-theme', newTheme);

    // Salvar preferência
    localStorage.setItem('theme', newTheme);
}