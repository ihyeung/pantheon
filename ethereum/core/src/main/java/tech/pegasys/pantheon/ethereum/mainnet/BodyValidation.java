/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.mainnet;

import static tech.pegasys.pantheon.crypto.Hash.keccak256;
import static tech.pegasys.pantheon.util.bytes.BytesValues.trimLeadingZeros;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.trie.MerklePatriciaTrie;
import tech.pegasys.pantheon.ethereum.trie.SimpleMerklePatriciaTrie;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.List;

/** A utility class for body validation tasks. */
public final class BodyValidation {

  private BodyValidation() {
    // Utility Class
  }

  private static BytesValue indexKey(final int i) {
    return RLP.encodeOne(trimLeadingZeros(UInt256.of(i).getBytes()));
  }

  private static MerklePatriciaTrie<BytesValue, BytesValue> trie() {
    return new SimpleMerklePatriciaTrie<>(b -> b);
  }

  /**
   * Generates the transaction root for a list of transactions
   *
   * @param transactions the transactions
   * @return the transaction root
   */
  public static Hash transactionsRoot(final List<Transaction> transactions) {
    final MerklePatriciaTrie<BytesValue, BytesValue> trie = trie();

    for (int i = 0; i < transactions.size(); ++i) {
      trie.put(indexKey(i), RLP.encode(transactions.get(i)::writeTo));
    }

    return Hash.wrap(trie.getRootHash());
  }

  /**
   * Generates the receipt root for a list of receipts
   *
   * @param receipts the receipts
   * @return the receipt root
   */
  public static Hash receiptsRoot(final List<TransactionReceipt> receipts) {
    final MerklePatriciaTrie<BytesValue, BytesValue> trie = trie();

    for (int i = 0; i < receipts.size(); ++i) {
      trie.put(indexKey(i), RLP.encode(receipts.get(i)::writeTo));
    }

    return Hash.wrap(trie.getRootHash());
  }

  /**
   * Generates the ommers hash for a list of ommer block headers
   *
   * @param ommers the ommer block headers
   * @return the ommers hash
   */
  public static Hash ommersHash(final List<BlockHeader> ommers) {
    return Hash.wrap(keccak256(RLP.encode(out -> out.writeList(ommers, BlockHeader::writeTo))));
  }

  /**
   * Generates the logs bloom filter for a list of transaction receipts
   *
   * @param receipts the transaction receipts
   * @return the logs bloom filter
   */
  public static LogsBloomFilter logsBloom(final List<TransactionReceipt> receipts) {
    final LogsBloomFilter logsBloom = new LogsBloomFilter();

    for (final TransactionReceipt receipt : receipts) {
      logsBloom.digest(receipt.getBloomFilter());
    }

    return logsBloom;
  }
}
