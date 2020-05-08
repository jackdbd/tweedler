(function iife(){
    const title = document.querySelector('input[name="title"]');
    const content = document.querySelector('textarea[name="content"]');
    const invalidFormClass = 'invalid-form-field';

    const handleTitleValidation = (event) => {
        if (title.validity.valueMissing) {
            title.setCustomValidity('Enter a title (4-50 characters)');
            title.classList.add(invalidFormClass);
        } else {
            title.setCustomValidity('');
            title.classList.remove(invalidFormClass);
        }
    }

    const handleContentValidation = (event) => {
        if (content.validity.valueMissing) {
            content.setCustomValidity('Enter some text (1-200 characters)');
            content.classList.add(invalidFormClass);
        } else {
            content.setCustomValidity('');
            content.classList.remove(invalidFormClass);
        }
    }
    
    title.addEventListener('input', handleTitleValidation);
    content.addEventListener('input', handleContentValidation);

    window.onbeforeunload = function(event) {
        title.removeEventListener('input', handleTitleValidation);
        content.removeEventListener('input', handleContentValidation);
    }
})();
