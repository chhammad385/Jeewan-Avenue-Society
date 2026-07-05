// Universal header loader with profile image functionality
// Include this script on any page that needs to load the header

function loadHeaderWithProfileImage() {
    fetch('header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('header').innerHTML = data;
            
            // Initialize header functionality
            if (typeof setActive === 'function') {
                setTimeout(setActive, 50);
            }
            if (typeof loadUserProfileImage === 'function') {
                setTimeout(loadUserProfileImage, 100);
            }
        })
        .catch(error => {
            console.error('Error loading header:', error);
        });
}

// Auto-load header when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('header')) {
        loadHeaderWithProfileImage();
    }
});
