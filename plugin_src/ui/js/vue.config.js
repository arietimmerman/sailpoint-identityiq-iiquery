module.exports = {

    filenameHashing: false,
    configureWebpack: {
        optimization: {
            splitChunks: false
        }
    },
    css: {
        extract: false,
    }
}
