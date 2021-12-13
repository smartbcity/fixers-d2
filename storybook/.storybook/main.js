const path = require("path");

module.exports = {
  stories: [
    "../docs/**/*.stories.mdx",
    "../docs/**/*.stories.@(js|jsx|ts|tsx)"
  ],
  addons: [
    {
      name: "@storybook/addon-links"
    },
    {
      name: "@storybook/addon-docs",
      options: {
        configureJSX: true,
        transcludeMarkdown: true
      }
    },
    {
      name: "@storybook/addon-essentials"
    }
  ],
  webpackFinal: async (config, { configType }) => {
    config.module.rules.push({
      test: /\.(kt|kts)$/,
      use: ["raw-loader"],
      include: path.resolve(__dirname, "../../")
    });

    // Return the altered config
    return config;
  }
};
