(function() {

    let button;

    Plugin.register('round_vertices_plugin', {
        title: 'Round Vertices',
        author: 'ietsnut',
        description: 'Rounds all mesh vertex positions to the nearest whole integer.',
        icon: 'fa-wand-magic-sparkles',
        version: '1.0.0',
        variant: 'both',

        onload() {
            button = new Action('round_vertices_action', {
                name: 'Round Vertices',
                description: 'Round all vertices.',
                icon: 'fa-wand-magic-sparkles',
                click: function() {
                    try {

                        Undo.initEdit({ elements: Mesh.all });

                        Mesh.all.forEach((mesh, meshIndex) => {
                            mesh.forAllFaces((face, faceKey) => {
                                face.vertices.forEach(vKey => {
                                    let coords = mesh.vertices[vKey];
                                    if (coords) {
                                        coords[0] = Math.round(coords[0]);
                                        coords[1] = Math.round(coords[1]);
                                        coords[2] = Math.round(coords[2]);
                                    }
                                });
                            });
                        });

                        Undo.finishEdit('Round Mesh Vertices');

                        Canvas.updateAll();
                        Canvas.updateAllBones();
                        Canvas.updateAllFaces();
                        Canvas.updateAllPositions();

                        Blockbench.showMessage('Rounded all vertices.', 'center');
                        
                    } catch (err) {
                        console.error('[Round Mesh Vertices] Error during rounding:', err);
                        Blockbench.showMessage('Error: ' + err.message, 'center');
                    }
                }
            });

            // Add the button to the Filter menu
            MenuBar.addAction(button, 'filter');
        },

        onunload() {
            button.delete();
        }
    });

})();
