(function(angular) {
    'use strict';
    angular
        .module('FileManagerApp')
        .provider('fileManagerConfig', fileManagerConfig);

    function fileManagerConfig() {

        var values = {
            appName: 'MAX File Sharing Service',
            defaultLang: 'en',

            listUrl: 'manage/listUrl',
            listAdminUrl: 'manage/listUrl',
            uploadUrl: 'manage/uploadUrl',
            renameUrl: 'manage/renameUrl',
            copyUrl: 'manage/copyUrl',
            removeUrl: 'manage/removeUrl',
            editUrl: 'manage/editUrl',
            reactivateUrl: 'manage/reactivateUrl',
            getContentUrl: 'manage/getContentUrl',
            createFolderUrl: 'manage/createFolderUrl',
            downloadFileUrl: 'manage/downloadFileUrl',
            compressUrl: 'manage/compressUrl',
            extractUrl: 'manage/extractUrl',
            permissionsUrl: 'manage/permissionsUrl',
            expirationDateUrl: 'manage/expirationDateUrl',

            sidebar: true,
            onlyFolders: false,
            breadcrumb: true,
            allowedActions: {
                rename: true,
                copy: true,
                edit: true,
                changePermissions: true,
                compress: true,
                compressChooseName: true,
                extract: true,
                download: true,
                preview: true,
                remove: true
            },

            enablePermissionsRecursive: true,
            compressAsync: true,
            extractAsync: true,

            isEditableFilePattern: /\.(txt|html?|aspx?|ini|pl|py|md|css|js|log|htaccess|htpasswd|json|sql|xml|xslt?|sh|rb|as|bat|cmd|coffee|php[3-6]?|java|c|cbl|go|h|scala|vb)$/i,
            isImageFilePattern: /\.(jpe?g|gif|bmp|png|svg|tiff?)$/i,
            isExtractableFilePattern: /\.(gz|tar|rar|g?zip)$/i,
            tplPath: 'src/templates'
        };

        return { 
            $get: function() {
                return values;
            }, 
            set: function (constants) {
                angular.extend(values, constants);
            }
        };
    
    }

})(angular);
