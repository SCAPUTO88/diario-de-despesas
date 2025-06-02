// Gerenciador de Animações
const AnimationManager = {
    // Loading Spinner
    showLoading() {
        const loading = document.querySelector('.loading-container');
        if (loading) loading.classList.add('active');
    },

    hideLoading() {
        const loading = document.querySelector('.loading-container');
        if (loading) loading.classList.remove('active');
    },

    // Toast Notifications
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="ti ${
                type === 'success' ? 'ti-check' :
                type === 'error' ? 'ti-x' :
                type === 'warning' ? 'ti-alert-triangle' : 'ti-info-circle'
            }"></i>
            <span>${message}</span>
        `;

        const container = document.querySelector('.toast-container') ||
            (() => {
                const cont = document.createElement('div');
                cont.className = 'toast-container';
                document.body.appendChild(cont);
                return cont;
            })();

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'fadeOut 0.3s ease-out forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },

    // Button Loading State
    setButtonLoading(button, isLoading) {
        if (isLoading) {
            button.classList.add('loading');
            button.setAttribute('disabled', 'disabled');
            button._originalText = button.innerHTML;
            button.innerHTML = '<span class="spinner"></span> Processando...';
        } else {
            button.classList.remove('loading');
            button.removeAttribute('disabled');
            if (button._originalText) {
                button.innerHTML = button._originalText;
            }
        }
    }
};

// Page Transition Handler
document.addEventListener('DOMContentLoaded', () => {
    // Adiciona classe de transição ao conteúdo principal
    const mainContent = document.querySelector('main');
    if (mainContent) {
        mainContent.classList.add('page-transition');
    }

    // Adiciona container de loading ao body
    if (!document.querySelector('.loading-container')) {
        const loadingContainer = document.createElement('div');
        loadingContainer.className = 'loading-container';
        loadingContainer.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(loadingContainer);
    }

    // Intercepta submissões de formulário para mostrar loading
    document.addEventListener('submit', (e) => {
        const form = e.target;
        if (form.classList.contains('needs-loading')) {
            e.preventDefault();
            const submitButton = form.querySelector('[type="submit"]');
            if (submitButton) {
                AnimationManager.setButtonLoading(submitButton, true);
            }
            AnimationManager.showLoading();

            // Simula delay para demonstração
            setTimeout(() => {
                form.submit();
            }, 1000);
        }
    });
});

// Exemplo de uso para feedback de ações
window.showActionFeedback = (action, success = true) => {
    const messages = {
        save: {
            success: 'Dados salvos com sucesso!',
            error: 'Erro ao salvar os dados.'
        },
        delete: {
            success: 'Item excluído com sucesso!',
            error: 'Erro ao excluir o item.'
        },
        update: {
            success: 'Dados atualizados com sucesso!',
            error: 'Erro ao atualizar os dados.'
        }
    };

    const message = messages[action]?.[success ? 'success' : 'error'] || 'Operação realizada.';
    AnimationManager.showToast(message, success ? 'success' : 'error');
};