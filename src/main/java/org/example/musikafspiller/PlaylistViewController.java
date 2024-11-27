package org.example.musikafspiller;

import lombok.Getter;
import lombok.Setter;

public class PlaylistViewController {
    @Setter
    @Getter
    private Playlist playlist;

    @Setter
    private UserLibrary userLibrary;
}
