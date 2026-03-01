import { test, expect } from '../fixtures/auth';

// ─── Admin-only routes ────────────────────────────────────────────────────────

test.describe('Admin-only routes', () => {
  test('admin can access /dashboard/users', async ({ adminPage: page }) => {
    await page.goto('/dashboard/users');
    await expect(page).toHaveURL(/dashboard\/users/);
    // The users list should be visible
    await expect(page.locator('app-users-list, .users-list, h1')).toBeVisible();
  });

  test('regular user is redirected away from /dashboard/users', async ({
    userPage: page,
  }) => {
    await page.goto('/dashboard/users');
    // adminGuard should redirect to /dashboard
    await expect(page).toHaveURL(/\/dashboard$/);
    await expect(page).not.toHaveURL(/users/);
  });

  test('admin can access the guide creation form', async ({ adminPage: page }) => {
    await page.goto('/dashboard/guides/new');
    await expect(page).toHaveURL(/guides\/new/);
  });

  test('regular user is redirected away from guide creation', async ({
    userPage: page,
  }) => {
    await page.goto('/dashboard/guides/new');
    await expect(page).not.toHaveURL(/guides\/new/);
  });
});

// ─── Guide visibility ─────────────────────────────────────────────────────────

test.describe('Guide visibility', () => {
  test('admin sees all guides', async ({ adminPage: page }) => {
    await page.goto('/dashboard/guides');
    // Admin should see a list (even if empty)
    await expect(page).toHaveURL(/guides/);
  });

  test('user can reach the guides list', async ({ userPage: page }) => {
    await page.goto('/dashboard/guides');
    await expect(page).toHaveURL(/guides/);
  });
});

// ─── Profile ─────────────────────────────────────────────────────────────────

test.describe('Profile page', () => {
  test('admin can access their profile', async ({ adminPage: page }) => {
    await page.goto('/dashboard/profile');
    await expect(page).toHaveURL(/profile/);
  });

  test('user can access their profile', async ({ userPage: page }) => {
    await page.goto('/dashboard/profile');
    await expect(page).toHaveURL(/profile/);
  });
});
