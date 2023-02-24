module.exports = {
  stories: [
    "../**/*.stories.mdx"
  ],
  addons: [
    {
      name: "@storybook/addon-docs",
      options: {
        configureJSX: true,
        transcludeMarkdown: true,
      },
    },
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    "storybook-react-i18next",
  ],
  features: {
    emotionAlias: false,
  },
  staticDirs: ["./public"]
};
