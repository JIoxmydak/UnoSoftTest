package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        String fileName = args[0];

        List<String[]> lines = getLines(fileName);
        List<Set<String[]>> groups = findGroups(lines);
        writeGroups(groups, "result.txt");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println(elapsedTime);
    }

    private static List<String[]> getLines(String fileName) throws IOException {
        List<String[]> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!isValidLine(line)) continue;

                String[] splitLine = line.split(";");
                for (int i = 0; i < splitLine.length; i++) {
                    splitLine[i] = splitLine[i].replace("\"","").trim();
                }

                lines.add(splitLine);
            }
        }

        return lines;
    }

    private static boolean isValidLine(String line) {
        int quoteCount = 0;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                quoteCount++;
            }
        }

        return quoteCount % 2 == 0;
    }

    private static List<Set<String[]>> findGroups(List<String[]> lines) {
        int n = lines.size();
        UnionFind uf = new UnionFind(n);

        // Map to track column values and their corresponding indices
        Map<String, List<Integer>> valueToIndicesMap = new HashMap<>();

        for (int col = 0; col < lines.get(0).length; col++) {
            valueToIndicesMap.clear();
            for (int i = 0; i < n; i++) {
                if (col >= lines.get(i).length) continue; // Avoid out of bounds access
                String value = lines.get(i)[col];
                if (!value.isEmpty()) {
                    valueToIndicesMap.putIfAbsent(value, new ArrayList<>());
                    valueToIndicesMap.get(value).add(i);
                }
            }
            for (List<Integer> indices : valueToIndicesMap.values()) {
                for (int i = 0; i < indices.size(); i++) {
                    for (int j = i + 1; j < indices.size(); j++) {
                        uf.union(indices.get(i), indices.get(j));
                    }
                }
            }
        }

        // Grouping lines based on their root in UnionFind structure
        Map<Integer, Set<String[]>> groupMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            groupMap.putIfAbsent(root, new HashSet<>());
            groupMap.get(root).add(lines.get(i));
        }

        return new ArrayList<>(groupMap.values());
    }

    private static void writeGroups(List<Set<String[]>> groups, String fileName) throws IOException {
        groups.sort((g1, g2) -> Integer.compare(g2.size(), g1.size()));
        int groupsAmount = (int) groups.stream().filter(g -> g.size() > 1).count();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Число групп с более чем одним элементом: " + groupsAmount);
            writer.newLine();

            int groupNumber = 1;
            for (Set<String[]> group : groups) {
                if (group.size() > 1) {
                    writer.write("Группа " + groupNumber);
                    writer.newLine();

                    for (String[] line : group) {
                        writer.write(String.join(";", line));
                        writer.newLine();
                    }

                    writer.newLine();
                    groupNumber++;
                }
            }
        }
    }
}