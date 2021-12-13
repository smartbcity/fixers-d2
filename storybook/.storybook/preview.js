import React from "react";
import {
  ThemeContextProvider,
  defaultMaterialUiTheme
} from "@smartb/g2-themes";
import { StorybookCanvas } from "@smartb/storybook-documentation";
import { ThemeProvider as EmotionThemeProvider } from "emotion-theming";

export const parameters = {
  options: {
    storySort: {
      order: [
        "Overview",
        ["Getting started", "Domain"],
        "Swagger",
        "View",
        "S2"
      ]
    }
  },
  docs: {
    container: StorybookCanvas
  }
};

const defaultTheme = {
  name: "default",
  colors: {
    primary: "#EDBA27",
    secondary: "#353945",
    tertiary: "#e0e0e0",
    error: "#E44258",
    success: "#00CA72",
    warning: "#FF9900",
    info: "#3C78D8"
  },
  shadows: [
    "none",
    "0px 4px 8px rgba(0, 0, 0, 0.2)",
    "0px 5px 12px rgba(0, 0, 0, 0.21)",
    "0px 6px 16px rgba(0, 0, 0, 0.22)",
    "0px 7px 20px rgba(0, 0, 0, 0.23)",
    "0px 8px 24px rgba(0, 0, 0, 0.24)",
    "0px 9px 28px rgba(0, 0, 0, 0.25)",
    "0px 10px 32px rgba(0, 0, 0, 0.26)",
    "0px 11px 36px rgba(0, 0, 0, 0.27)",
    "0px 12px 40px rgba(0, 0, 0, 0.28)",
    "0px 13px 44px rgba(0, 0, 0, 0.29)",
    "0px 14px 48px rgba(0, 0, 0, 0.3)",
    "0px 15px 52px rgba(0, 0, 0, 0.31)"
  ]
};

export const withThemeProvider = Story => {
  return (
    <EmotionThemeProvider theme={defaultMaterialUiTheme(defaultTheme)}>
      <ThemeContextProvider theme={defaultTheme}>
        {Story()}
      </ThemeContextProvider>
    </EmotionThemeProvider>
  );
};

export const decorators = [withThemeProvider];
