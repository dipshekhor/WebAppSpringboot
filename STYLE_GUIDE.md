# 🎨 AquaEdu Visual Style Guide

## Quick Reference for Developers

### Color Codes (Copy & Paste)

#### Primary Palette
```
Primary:        #0891B2  (Cyan 600)
Primary Dark:   #0E7490  (Cyan 700)
Secondary:      #1E40AF  (Blue 800)
Accent:         #F97316  (Orange 500)
```

#### Semantic Colors
```
Success:        #059669  (Emerald 600)
Warning:        #D97706  (Amber 600)
Danger:         #E11D48  (Rose 600)
```

#### Light Mode
```
Background:     #F8FAFC  (Slate 50)
Card:           #FFFFFF  (White)
Text:           #0F172A  (Slate 900)
Border:         #E2E8F0  (Slate 200)
```

#### Dark Mode
```
Background:     #0F172A  (Slate 900)
Card:           #1E293B  (Slate 800)
Text:           #F8FAFC  (Slate 50)
Border:         #334155  (Slate 700)
```

---

## Typography Quick Reference

```css
/* Headings */
Hero:           2.25rem (36px) - font-weight: 800
Section:        1.875rem (30px) - font-weight: 700
Card Title:     1.5rem (24px) - font-weight: 700
Subheading:     1.25rem (20px) - font-weight: 600

/* Body Text */
Large:          1.125rem (18px) - font-weight: 500
Normal:         1rem (16px) - font-weight: 400
Small:          0.875rem (14px) - font-weight: 400

/* UI Elements */
Buttons:        1rem (16px) - font-weight: 700
Badges:         0.75rem (12px) - font-weight: 700, uppercase
```

---

## Spacing Reference

```
Tight:    4px, 8px           (xs, sm)
Normal:   16px, 24px         (md, lg)
Loose:    32px, 48px, 64px   (xl, 2xl, 3xl)
```

### Common Patterns
```css
Button padding:         16px 32px
Input padding:          16px 24px
Card padding:           32px
Modal padding:          40px
Section margin:         48px
```

---

## Border Radius Quick Guide

```css
Small elements:    8px   (.5rem)
Buttons/Inputs:    16px  (1rem)
Cards:             24px  (1.5rem)
Modals:            32px  (2rem)
Badges/Pills:      9999px (full)
```

---

## Shadow Classes

```css
/* Elevation */
sm:      0 1px 3px rgba(0,0,0,0.1)
md:      0 4px 6px rgba(0,0,0,0.1)
lg:      0 10px 20px rgba(0,0,0,0.1)
xl:      0 20px 40px rgba(0,0,0,0.15)

/* Brand Glows */
primary: 0 10px 40px -10px rgba(8,145,178,0.4)
accent:  0 10px 40px -10px rgba(249,115,22,0.4)
```

---

## Common UI Patterns

### Primary Button
```html
<button class="px-6 py-3 bg-gradient-to-r from-primary to-secondary text-white rounded-2xl font-bold shadow-primary hover:shadow-2xl hover:scale-105 transition-all">
    Click Me
</button>
```

### Card
```html
<div class="bg-white dark:bg-card-dark rounded-3xl p-10 shadow-xl border-2 border-slate-200 dark:border-slate-700/50">
    Content
</div>
```

### Badge
```html
<span class="px-3 py-1.5 bg-gradient-to-r from-primary/10 to-primary/20 text-primary font-bold rounded-full border border-primary/30 text-xs uppercase">
    Label
</span>
```

### Input Field
```html
<input type="text" class="w-full px-5 py-4 bg-slate-50 dark:bg-input-dark rounded-2xl border-2 border-slate-200 dark:border-slate-600 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all font-medium" placeholder="Enter text">
```

---

## Icon Sizes

```
Small:    16px-20px    (List items, inline)
Medium:   24px-28px    (Buttons, navigation)
Large:    32px-40px    (Feature cards)
XLarge:   48px-64px    (Hero sections, avatars)
```

---

## Gradient Combinations

### Primary Gradient
```css
background: linear-gradient(135deg, #0891B2 0%, #1E40AF 100%);
```

### Success Gradient
```css
background: linear-gradient(135deg, #059669 0%, #047857 100%);
```

### Danger Gradient
```css
background: linear-gradient(135deg, #E11D48 0%, #BE123C 100%);
```

### Accent Gradient
```css
background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
```

---

## Animation Timing

```css
Fast:     150ms ease   (Hover states)
Normal:   300ms ease   (Transitions)
Slow:     500ms ease   (Page loads)
```

---

## Entity Color Coding

```
Teachers:  Teal/Cyan     (#0891B2)
Students:  Emerald       (#059669)
Courses:   Amber         (#F97316)
Admin:     Blue          (#1E40AF)
Alerts:    Rose          (#E11D48)
```

---

## Responsive Breakpoints

```
Mobile:    < 768px      (sm)
Tablet:    768-1024px   (md)
Desktop:   > 1024px     (lg)
```

---

## Z-Index Layers

```
Base:           0
Dropdown:       10
Sticky:         20
Modal Overlay:  1000
Modal Content:  1001
Toast:          2000
Tooltip:        3000
```

---

## Common Tailwind Classes

### Layout
```
Container:     max-w-6xl mx-auto px-4
Flex Center:   flex items-center justify-center
Grid 2 Col:    grid grid-cols-2 gap-4
```

### Spacing
```
Padding:       p-4, p-8, p-10
Margin:        m-4, mb-8, mt-10
Gap:           space-x-4, space-y-6
```

### Colors
```
Primary:       bg-primary, text-primary, border-primary
Text Light:    text-slate-600 dark:text-slate-400
Background:    bg-white dark:bg-card-dark
```

---

## Accessibility Checklist

- ✅ Color contrast ratio > 4.5:1
- ✅ Focus visible indicators
- ✅ Keyboard navigation
- ✅ ARIA labels on icons
- ✅ Semantic HTML
- ✅ Alt text on images
- ✅ Responsive design
- ✅ Touch targets > 44px

---

## File Structure

```
/static/
  ├── index.html       (Main UI)
  ├── app.js           (JavaScript logic)
  └── style.css        (Design system CSS)
```

---

## Quick Start Commands

```bash
# View in browser
open http://localhost:8081

# Login credentials
Admin:   admin / adminpass
Student: user / userpass
```

---

## Brand Assets

```
Logo Text:     AquaEdu
Font:          Poppins
Icon Style:    Material Symbols (Filled)
Primary Icon:  "A" in rounded square
```

---

## Common Issues & Solutions

**Issue**: Colors not showing  
**Solution**: Ensure Tailwind CDN is loaded

**Issue**: Fonts not loading  
**Solution**: Check Google Fonts preconnect

**Issue**: Dark mode not working  
**Solution**: Add `class="dark"` to `<html>` element

**Issue**: Gradients not smooth  
**Solution**: Use multiple color stops in gradient

---

## Performance Tips

1. Use CSS transforms (translate, scale) for animations
2. Avoid animating width/height
3. Use `will-change` sparingly
4. Optimize images before upload
5. Lazy load off-screen content

---

**Quick Links**:
- [Full Documentation](./DESIGN_SYSTEM.md)
- [Tailwind Docs](https://tailwindcss.com)
- [Material Icons](https://fonts.google.com/icons)
- [Poppins Font](https://fonts.google.com/specimen/Poppins)
