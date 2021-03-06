package org.tagpro.bots;

import org.tagpro.tasc.data.BallUpdate;
import org.tagpro.tasc.data.Key;
import org.tagpro.tasc.data.KeyState;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class GamePadTest {

    @Test
    public void testGoToLeftFromStandstill() throws Exception {
        MockGamePad c = new MockGamePad();
        c.goTo(1f, 1f, new BallUpdate(1, 8f, 1f, -1f, 0f, 0f, 0f));
        final Map<Key, KeyState> expected = new HashMap<>();
        expected.put(Key.LEFT, KeyState.KEYDOWN);
        expected.put(Key.UP, KeyState.KEYUP);
        Assert.assertEquals(c.getPressedKeys(), expected);
    }

    @Test
    public void testGoToLeftUpFromToMuchDownSpeed() throws Exception {
        MockGamePad c = new MockGamePad();
        c.goTo(1f, 1f, new BallUpdate(1, 8.4f, 3.6f, -1f, 5f, 0f, 0f));
        final Map<Key, KeyState> expected = new HashMap<>();
        expected.put(Key.LEFT, KeyState.KEYDOWN);
        expected.put(Key.UP, KeyState.KEYDOWN);
        Assert.assertEquals(c.getPressedKeys(), expected);
    }

    @Test
    public void testGoToLeftUpFromToMuchUpSpeed() throws Exception {
        MockGamePad c = new MockGamePad();
        c.goTo(1f, 1f, new BallUpdate(1, 8.4f, 3.6f, -1f, -5f, 0f, 0f));
        final Map<Key, KeyState> expected = new HashMap<>();
        expected.put(Key.LEFT, KeyState.KEYDOWN);
        expected.put(Key.UP, KeyState.KEYUP);
        Assert.assertEquals(c.getPressedKeys(), expected);
    }

    @Test
    public void testGoToLeftDownFromToMuchDownSpeed() throws Exception {
        MockGamePad c = new MockGamePad();
        c.goTo(1f, 5f, new BallUpdate(1, 8.4f, 3.6f, -1f, 5f, 0f, 0f));
        final Map<Key, KeyState> expected = new HashMap<>();
        expected.put(Key.LEFT, KeyState.KEYDOWN);
        expected.put(Key.DOWN, KeyState.KEYUP);
        Assert.assertEquals(c.getPressedKeys(), expected);
    }

    @Test
    public void testGoToLeftDownFromToMuchUpSpeed() throws Exception {
        MockGamePad c = new MockGamePad();
        c.goTo(1f, 5f, new BallUpdate(1, 8.4f, 3.6f, -1f, -5f, 0f, 0f));
        final Map<Key, KeyState> expected = new HashMap<>();
        expected.put(Key.LEFT, KeyState.KEYDOWN);
        expected.put(Key.DOWN, KeyState.KEYDOWN);
        Assert.assertEquals(c.getPressedKeys(), expected);
    }


    private class MockGamePad extends GamePad {
        private Map<Key, KeyState> pressedKeys = new HashMap<>();

        public MockGamePad() {
            super(null, null);
        }

        @Override
        public void key(Key key, KeyState keyState) {
            pressedKeys.put(key, keyState);
        }

        public Map<Key, KeyState> getPressedKeys() {
            return pressedKeys;
        }
    }
}